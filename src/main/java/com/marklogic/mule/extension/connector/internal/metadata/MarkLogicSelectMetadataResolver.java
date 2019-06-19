/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2019 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.metadata;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

/**
 *
 *
 * @since 1.0.1
 *
 */
public class MarkLogicSelectMetadataResolver implements OutputTypeResolver<String>
{

    @Override
    public String getResolverName()
    {
        return "MarkLogicSelectResolver";
    }

    @Override
    public MetadataType getOutputType(MetadataContext metadataContext, String s) throws MetadataResolvingException, ConnectionException
    {
        return null;
    }

    @Override
    public String getCategoryName()
    {
        return "MarkLogicCategory";
    }
}
