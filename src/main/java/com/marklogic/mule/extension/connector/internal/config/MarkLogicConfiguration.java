/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2020 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.config;

import com.marklogic.client.document.ServerTransform;
import com.marklogic.mule.extension.connector.internal.operation.MarkLogicOperations;
import com.marklogic.mule.extension.connector.internal.connection.provider.MarkLogicConnectionProvider;
import com.marklogic.mule.extension.connector.internal.error.exception.MarkLogicConnectorException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension configuration, values set in this class
 * are commonly used across multiple operations since they represent something
 * core from the extension.
 */
@Operations(MarkLogicOperations.class)
@ConnectionProviders(MarkLogicConnectionProvider.class)
public class MarkLogicConfiguration
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConfiguration.class);

    @DisplayName("Connection ID")
    @Parameter
    @Summary("An identifier used for the Mulesoft Connector to keep state of its connection to MarkLogic.")
    @Example("testConfig-223efe")
    private String configId;

    @Parameter
    @Summary("The thread count passed to DMSDK, representing the number of parallel processing threads.")
    @Example("4")
    private int threadCount;

    @Parameter
    @Summary("The batch size passed to DMSDK, representing the number of documents processed within a batch.")
    @Example("100")
    private int batchSize;

    @Parameter
    @Summary("The name of an already registered and deployed MarkLogic server-side Javascript, XQuery, or XSLT module.")
    @Optional(defaultValue = "null")
    @Example("ml:sjsInputFlow")
    private String serverTransform;

    @Parameter
    @Summary("A comma-separated list of alternating transform parameter names and transform parameter values.")
    @Optional(defaultValue = "null")
    @Example("entity-name,MyEntity,flow-name,loadMyEntity")
    private String serverTransformParams;

    @Parameter
    @Summary("The number of seconds before DMSDK automatically flushes the current batch if not yet filled to the specified batchSize configurable.")
    @Example("2")
    private int secondsBeforeFlush;

    @Parameter
    @Summary("The job name used by DMSDK to track the job.")
    @Example("myJobName")
    private String jobName;

    public String getConfigId()
    {
        return configId;
    }

    public int getThreadCount()
    {
        return threadCount;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public String getServerTransform()
    {
        return serverTransform;
    }

    public String getServerTransformParams()
    {
        return serverTransformParams;
    }

    public int getSecondsBeforeFlush()
    {
        return secondsBeforeFlush;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setConfigId(String configId)
    {
        this.configId = configId;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setServerTransform(String serverTransform)
    {
        this.serverTransform = serverTransform;
    }

    public void setServerTransformParams(String serverTransformParams)
    {
        this.serverTransformParams = serverTransformParams;
    }

    public void setSecondsBeforeFlush(int secondsBeforeFlush)
    {
        this.secondsBeforeFlush = secondsBeforeFlush;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public ServerTransform generateServerTransform(String transformName, String transformParams)
    {
        if (isDefined(transformName))
        {
            logger.debug("Transforming query doc payload with operation-defined transform: " + serverTransform);
            return this.createServerTransform(transformName, transformParams);
        }
        else if (isDefined(this.serverTransform))
        {
            logger.debug("Transforming query doc payload with connection-defined transform: " + this.getServerTransform());
            return createServerTransform(this.serverTransform, this.serverTransformParams);
        }
        else
        {
            logger.debug("Querying docs without a transform");
            return null;
        }
    }

    public static boolean isDefined(String str)
    {
        return str != null
                && !str.trim().isEmpty()
                && !"null".equalsIgnoreCase(str.trim());

    }

    private ServerTransform createServerTransform(String name, String params)
    {

        ServerTransform transform = new ServerTransform(name);

        if (isDefined(params))
        {
            List<String> pairs = Arrays.asList(params.split(","));
            int size = pairs.size();

            if (size % 2 != 0 || pairs.stream().anyMatch(it -> !isDefined(it)))
            {
                throw new MarkLogicConnectorException("Cannot create Server Transforms because params do not pair up");
            }

            for (int i = 0; i < size; i += 2)
            {
                transform.addParameter(pairs.get(i).trim(), pairs.get(i + 1).trim());
            }
        }

        return transform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MarkLogicConfiguration that = (MarkLogicConfiguration)o;
        return threadCount == that.threadCount &&
                batchSize == that.batchSize &&
                secondsBeforeFlush == that.secondsBeforeFlush &&
                Objects.equals(configId, that.configId) &&
                Objects.equals(serverTransform, that.serverTransform) &&
                Objects.equals(serverTransformParams, that.serverTransformParams) &&
                Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId, threadCount, batchSize, serverTransform, serverTransformParams, secondsBeforeFlush, jobName);
    }
}
