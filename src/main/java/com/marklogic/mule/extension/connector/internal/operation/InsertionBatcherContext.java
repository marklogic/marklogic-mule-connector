/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2023 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
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
