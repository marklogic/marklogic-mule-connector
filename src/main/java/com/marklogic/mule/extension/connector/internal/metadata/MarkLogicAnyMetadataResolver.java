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
package com.marklogic.mule.extension.connector.internal.metadata;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.AnyType;
import org.mule.metadata.api.model.BinaryType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import java.io.InputStream;

/**
 * MarkLogic "Any" {@link OutputTypeResolver} implementation for the basic operations that always return an {@link AnyType}, since MarkLogic
 * may return any object type like an XML, JSON, Binary, Text, RDF, etc.
 *
 * @since 1.1.1
 *
 */
public class MarkLogicAnyMetadataResolver extends OutputStaticTypeResolver
{

    private static final AnyType ANY_TYPE = BaseTypeBuilder.create(JAVA).anyType().build();
    
    @Override
    public MetadataType getStaticMetadata() {
        return ANY_TYPE;
    }

    @Override
    public String getCategoryName()
    {
        return "MarkLogicCategory";
    }
}
