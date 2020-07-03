package com.marklogic.mule.extension.connector.internal.config;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class DataHubIngestFlowOptions {
    @Parameter
    @Summary("The name of the flow to run.")
    private String flowName;

    @Parameter
    @Summary("The ingestion flow step number.")
    @Example("1")
    private int flowStep;

    @Parameter
    @Optional
    @Summary("The data hub job ID.")
    @Example("mule-job-0001")
    private String jobId;

    public String getFlowName() { return flowName; }

    public int getFlowStep() { return flowStep; }

    public String getJobId() { return jobId; }

    public void setFlowName(String flowName) { this.flowName = flowName; }

    public void setFlowStep(int flowStep) { this.flowStep = flowStep; }

    public void setJobId(String jobId) { this.jobId = jobId; }
}
