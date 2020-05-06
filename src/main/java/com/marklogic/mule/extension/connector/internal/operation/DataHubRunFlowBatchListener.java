package com.marklogic.mule.extension.connector.internal.operation;

import com.marklogic.client.datamovement.WriteBatch;
import com.marklogic.client.datamovement.WriteBatchListener;
import com.marklogic.client.datamovement.WriteEvent;
import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.flow.FlowInputs;
import com.marklogic.hub.flow.FlowRunner;
import com.marklogic.hub.flow.RunFlowResponse;
import com.marklogic.hub.flow.impl.FlowRunnerImpl;
import com.marklogic.mule.extension.connector.internal.config.DataHubConfiguration;
import com.marklogic.mule.extension.connector.internal.config.DataHubRunFlowOptions;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import java.util.HashMap;
import java.util.Map;

public class DataHubRunFlowBatchListener extends LoggingObject implements WriteBatchListener {
    private MarkLogicConnection connection;
    private DataHubConfiguration dataHubConfiguration;
    private DataHubRunFlowOptions runFlowOptions;
    private HubConfig hubConfig;
    private boolean logResponse;

    public DataHubRunFlowBatchListener(MarkLogicConnection connection, DataHubConfiguration dataHubConfiguration, DataHubRunFlowOptions runFlowOptions) {
        this.connection = connection;
        this.dataHubConfiguration = dataHubConfiguration;
        this.runFlowOptions = runFlowOptions;
        this.hubConfig = connection.createHubConfig(dataHubConfiguration);
    }

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

        FlowRunner flowRunner = new FlowRunnerImpl(hubConfig);
        RunFlowResponse response = flowRunner.runFlow(inputs);
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
