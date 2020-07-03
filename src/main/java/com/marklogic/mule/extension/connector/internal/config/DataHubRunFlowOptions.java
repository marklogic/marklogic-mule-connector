package com.marklogic.mule.extension.connector.internal.config;

import org.mule.runtime.extension.api.annotation.param.Optional;
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
    @Example("2,3,4")
    private String flowSteps;

    @Parameter
    @Summary("The thread count passed to the flow runner, representing the number of parallel processing threads.")
    @Example("4")
    private int threadCount;

    @Parameter
    @Summary("The batch size passed to the flow runner, representing the number of documents processed within a batch.")
    @Example("100")
    private int batchSize;

    @Parameter
    @Optional
    @Summary("The data hub job ID.")
    @Example("mule-job-0001")
    private String jobId;

    public String getFlowName() { return flowName; }

    public String getFlowSteps() { return flowSteps; }

    public int getThreadCount() { return threadCount; }

    public int getBatchSize() { return batchSize; }

    public String getJobId() { return jobId; }

    public void setFlowName(String flowName) { this.flowName = flowName; }

    public void setFlowSteps(String flowSteps) { this.flowSteps = flowSteps; }

    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public void setJobId(String jobId) { this.jobId = jobId; }

    public List<String> getFlowStepsAsList() {
        return Arrays.asList(flowSteps.split(","));
    }
}
