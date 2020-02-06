package com.marklogic.mule.extension.connector.internal.operation;

import com.marklogic.client.datamovement.WriteBatch;
import com.marklogic.client.datamovement.WriteBatchListener;
import com.marklogic.client.datamovement.WriteEvent;
import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.hub.DatabaseKind;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.flow.FlowInputs;
import com.marklogic.hub.flow.FlowRunner;
import com.marklogic.hub.flow.RunFlowResponse;
import com.marklogic.hub.flow.impl.FlowRunnerImpl;
import com.marklogic.mule.extension.connector.internal.config.DataHubConfiguration;
import com.marklogic.mule.extension.connector.internal.config.RunFlowWithIngestOptions;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import java.util.HashMap;
import java.util.Map;

public class RunFlowWriteBatchListener extends LoggingObject implements WriteBatchListener {
    private MarkLogicConnection connection;
    private DataHubConfiguration dataHubConfiguration;
    private RunFlowWithIngestOptions runFlowOptions;
    private boolean logResponse;

    /**
     * The flowName and steps are assumed to have been read in by the client that is reading from system configuration
     * - in the Kafka case, this will be from the Kafka config map that is passed to a source task.
     * <p>
     * The DatabaseClientConfig object is needed because it's not yet possible for DHF to reuse the DatabaseClient that
     * Kafka constructs. While it's assumed that that DatabaseClient will write to staging, DHF needs to be able to
     * connect to the staging, final, and job app servers. And in order to do that, it needs all of the authentication
     * information that can be held by a DatabaseClientConfig. Though as of 5.2.0, DHF only supports basic/digest
     * authentication, and thus it's assumed that username/password will be used for authentication.
     *
     * @param connection            describes how to connect to MarkLogic
     * @param dataHubConfiguration  describes how the data hub is configured
     * @param runFlowOptions        describes which flow and steps to run
     */
    public RunFlowWriteBatchListener(MarkLogicConnection connection, DataHubConfiguration dataHubConfiguration, RunFlowWithIngestOptions runFlowOptions) {
        this.connection = connection;
        this.dataHubConfiguration = dataHubConfiguration;
        this.runFlowOptions = runFlowOptions;
    }

    /**
     * None of this is specific to Kafka. It assumes a pattern of - given the URIs that were just ingested (and are
     * available in the given WriteBatch), override the source query for each step to be executed with a document query
     * that constrains on those URIs.
     * <p>
     * The need to construct a source query is unfortunate. When DHF executes a non-ingestion step, it always runs the
     * collector. Thus, it's not yet possible to tell DHF - just process these URIs (specifically, it's not yet
     * possible to do that via FlowRunner). So it's necessary to use the URIs to construct a document query and override
     * each step's source query with that. Ideally, DHF can be enhanced here so a client can just pass in the URIs to
     * process, and then there's no call to the collector nor need to override the source query.
     *
     * @param batch
     */
    @Override
    public void processEvent(WriteBatch batch) {
        FlowInputs inputs = new FlowInputs(runFlowOptions.getFlowName());
        inputs.setSteps(runFlowOptions.getFlowStepsAsList());
        inputs.setJobId(batch.getBatcher().getJobId() + "-" + batch.getJobBatchNumber());

        Map<String, Object> options = new HashMap<>();
        options.put("sourceQuery", buildSourceQuery(batch));
        inputs.setOptions(options);

        Map<String, Object> stepConfig = new HashMap<>();
        stepConfig.put("threadCount", runFlowOptions.getThreadCount());
        stepConfig.put("batchSize", runFlowOptions.getBatchSize());
        inputs.setStepConfig(stepConfig);

        // DHF 5.2.0 only supports basic/digest auth, so this can safely be done.
        HubConfig hubConfig = connection.createDataHubConfig();
        hubConfig.setPort(DatabaseKind.STAGING, dataHubConfiguration.getStagingPort());
        hubConfig.setDbName(DatabaseKind.STAGING, dataHubConfiguration.getStagingDbName());
        hubConfig.setPort(DatabaseKind.FINAL, dataHubConfiguration.getFinalPort());
        hubConfig.setDbName(DatabaseKind.FINAL, dataHubConfiguration.getFinalDbName());
        hubConfig.setPort(DatabaseKind.JOB, dataHubConfiguration.getJobsPort());
        hubConfig.setDbName(DatabaseKind.JOB, dataHubConfiguration.getJobsDbName());

        FlowRunner flowRunner = new FlowRunnerImpl(hubConfig);
        RunFlowResponse response = flowRunner.runFlowWithoutProject(inputs);
        flowRunner.awaitCompletion();
        if (logResponse) {
            logger.info(format("Flow response for batch number %d:\n%s", batch.getJobBatchNumber(), response.toJson()));
        }
    }

    protected String buildSourceQuery(WriteBatch batch) {
        StringBuilder sb = new StringBuilder("cts.documentQuery([");
        boolean firstOne = true;
        for (WriteEvent event : batch.getItems()) {
            if (!firstOne) {
                sb.append(",");
            }
            sb.append(String.format("'%s'", event.getTargetUri()));
            firstOne = false;
        }
        return sb.append("])").toString();
    }

    public void setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
    }
}