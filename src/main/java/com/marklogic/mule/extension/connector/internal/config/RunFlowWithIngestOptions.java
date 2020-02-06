package com.marklogic.mule.extension.connector.internal.config;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.LinkedList;
import java.util.List;

public class RunFlowWithIngestOptions extends RunFlowOptions {
    @Parameter
    @Summary("Indicates whether to use wrap documents in an envelope pattern (DHF default ingest transform).")
    @Optional(defaultValue = "true")
    private boolean envelopeOnIngest;

    @Parameter
    @Summary("Indicates the flow's ingestion step.  This is required if envelopeOnIngest is true.")
    @Optional(defaultValue = "")
    private String ingestionStep;

    public RunFlowWithIngestOptions() {
        envelopeOnIngest = true;
        ingestionStep = null;
    }

    public boolean getEnvelopeOnIngest() { return envelopeOnIngest; }

    public String getIngestionStep() { return ingestionStep; }

    public void setEnvelopeOnIngest(boolean envelopeOnIngest) { this.envelopeOnIngest = envelopeOnIngest; }

    public void setIngestionStep(String ingestionStep) { this.ingestionStep = ingestionStep; }

    @Override
    public List<String> getFlowStepsAsList() {
        List<String> flowSteps = new LinkedList<String>(super.getFlowStepsAsList());
        flowSteps.remove(ingestionStep); // do not include an ingestion step when running flows (it will trigger any mlcp related config)
        return flowSteps;
    }
}
