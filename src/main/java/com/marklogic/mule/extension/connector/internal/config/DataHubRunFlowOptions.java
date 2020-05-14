package com.marklogic.mule.extension.connector.internal.config;

import com.marklogic.client.document.ServerTransform;
import com.marklogic.mule.extension.connector.internal.error.exception.MarkLogicConnectorException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.Arrays;
import java.util.List;

public class DataHubRunFlowOptions {
    @Parameter
    @Summary("The name of the flow to run.")
    private String flowName;

    @Parameter
    @Summary("A comma-delimited list of steps to run")
    @Example("1,2,3")
    private String flowSteps;

    @Parameter
    @Summary("Indicates whether the 1st step in flowSteps is an ingestion step.")
    @Example("true")
    private boolean isFirstStepIngestion;

    @Parameter
    @Summary("The thread count passed to the flow runner, representing the number of parallel processing threads.")
    @Example("4")
    private int threadCount;

    @Parameter
    @Summary("The batch size passed to the flow runner, representing the number of documents processed within a batch.")
    @Example("100")
    private int batchSize;

    public String getFlowName() { return flowName; }

    public String getFlowSteps() { return flowSteps; }

    public boolean getIsFirstStepIngestion() { return isFirstStepIngestion; }

    public int getThreadCount() { return threadCount; }

    public int getBatchSize() { return batchSize; }

    public void setFlowName(String flowName) { this.flowName = flowName; }

    public void setFlowSteps(String flowSteps) { this.flowSteps = flowSteps; }

    public void setIsFirstStepIngestion(boolean value) { this.isFirstStepIngestion = value; }

    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public List<String> getFlowStepsAsList() {
        return Arrays.asList(flowSteps.split(","));
    }

    public ServerTransform getIngestionStepTransform() {
        if (!isFirstStepIngestion)
            throw new MarkLogicConnectorException("The run flow options doesn't indicate any ingestion step.");

        List<String> flowSteps = getFlowStepsAsList();
        if (flowSteps.size() == 0)
            throw new MarkLogicConnectorException("No flow step(s) declared.");

        ServerTransform transform = new ServerTransform("mlRunIngest");
        transform.addParameter("flow-name", flowName);
        transform.addParameter("step", getFlowStepsAsList().get(0));
        return transform;
    }
}
