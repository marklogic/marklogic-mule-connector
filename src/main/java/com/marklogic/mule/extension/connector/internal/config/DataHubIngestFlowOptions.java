package com.marklogic.mule.extension.connector.internal.config;

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

    public String getFlowName() { return flowName; }

    public int getFlowStep() { return flowStep; }

    public void setFlowName(String flowName) { this.flowName = flowName; }

    public void setFlowStep(int flowStep) { this.flowStep = flowStep; }
}
