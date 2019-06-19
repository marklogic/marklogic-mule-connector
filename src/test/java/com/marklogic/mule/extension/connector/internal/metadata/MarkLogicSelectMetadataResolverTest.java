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

import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataContext;

/**
 *
 * @author shingjo
 */
public class MarkLogicSelectMetadataResolverTest
{

    /**
     * Test of getResolverName method, of class MarkLogicSelectMetadataResolver.
     */
    @Test
    public void testGetResolverName()
    {
        MarkLogicSelectMetadataResolver instance = new MarkLogicSelectMetadataResolver();
        String expResult = "MarkLogicSelectResolver";
        String result = instance.getResolverName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOutputType method, of class MarkLogicSelectMetadataResolver.
     */
    @Test
    public void testGetOutputType() throws Exception
    {
        MetadataContext metadataContext = null;
        String s = "";
        MarkLogicSelectMetadataResolver instance = new MarkLogicSelectMetadataResolver();
        MetadataType expResult = null;
        MetadataType result = instance.getOutputType(metadataContext, s);
        assertEquals(expResult, result);

    }

    /**
     * Test of getCategoryName method, of class MarkLogicSelectMetadataResolver.
     */
    @Test
    public void testGetCategoryName()
    {
        MarkLogicSelectMetadataResolver instance = new MarkLogicSelectMetadataResolver();
        String expResult = "MarkLogicCategory";
        String result = instance.getCategoryName();
        assertEquals(expResult, result);
    }

}
