package com.marklogic.mule.extension.connector.internal.operation;

import com.marklogic.mule.extension.connector.internal.config.MarkLogicConfiguration;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import java.util.Objects;

/**
 * Captures the inputs and context needed for constructing a MarkLogicInsertionBatcher.
 */
public class InsertionBatcherContext {
    private MarkLogicConfiguration configuration;
    private MarkLogicConnection connection;
    private String outputCollections;
    private String outputPermissions;
    private int outputQuality;
    private String jobName;
    private String temporalCollection;
    private String serverTransform;
    private String serverTransformParams;

    public int computeSignature() {
        return Objects.hash(configuration,
            connection,
            outputCollections,
            outputPermissions,
            outputQuality,
            jobName,
            temporalCollection,
            serverTransform,
            serverTransformParams
        );
    }

    public MarkLogicConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MarkLogicConfiguration configuration) {
        this.configuration = configuration;
    }

    public MarkLogicConnection getConnection() {
        return connection;
    }

    public void setConnection(MarkLogicConnection connection) {
        this.connection = connection;
    }

    public String getOutputCollections() {
        return outputCollections;
    }

    public void setOutputCollections(String outputCollections) {
        this.outputCollections = outputCollections;
    }

    public String getOutputPermissions() {
        return outputPermissions;
    }

    public void setOutputPermissions(String outputPermissions) {
        this.outputPermissions = outputPermissions;
    }

    public int getOutputQuality() {
        return outputQuality;
    }

    public void setOutputQuality(int outputQuality) {
        this.outputQuality = outputQuality;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTemporalCollection() {
        return temporalCollection;
    }

    public void setTemporalCollection(String temporalCollection) {
        this.temporalCollection = temporalCollection;
    }

    public String getServerTransform() {
        return serverTransform;
    }

    public void setServerTransform(String serverTransform) {
        this.serverTransform = serverTransform;
    }

    public String getServerTransformParams() {
        return serverTransformParams;
    }

    public void setServerTransformParams(String serverTransformParams) {
        this.serverTransformParams = serverTransformParams;
    }
}
