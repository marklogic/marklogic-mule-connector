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
import com.marklogic.mule.extension.connector.internal.error.exception.MarkLogicConnectorException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jshingle
 */
public class MarkLogicConfigurationTest
{

    MarkLogicConfiguration instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new MarkLogicConfiguration();
    }

    /**
     * Test of getConfigId method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetConfigId()
    {
        String expResult = "configuration-id-test-123";
        instance.setConfigId(expResult);
        String result = instance.getConfigId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getThreadCount method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetThreadCount()
    {
        int expResult = 64;
        instance.setThreadCount(expResult);
        int result = instance.getThreadCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBatchSize method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetBatchSize()
    {
        int expResult = 250;
        instance.setBatchSize(expResult);
        int result = instance.getBatchSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerTransform method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetServerTransform()
    {
        String expResult = "TestTransform";
        instance.setServerTransform(expResult);
        String result = instance.getServerTransform();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerTransformParams method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetServerTransformParams()
    {
        String expResult = "entity-name,MyEntity,flow-name,loadMyEntity";
        instance.setServerTransformParams(expResult);
        String result = instance.getServerTransformParams();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSecondsBeforeFlush method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetSecondsBeforeFlush()
    {
        int expResult = 2;
        instance.setSecondsBeforeFlush(expResult);
        int result = instance.getSecondsBeforeFlush();
        assertEquals(expResult, result);
    }

    /**
     * Test of getJobName method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGetJobName()
    {
        String expResult = "TestJobName";
        instance.setJobName(expResult);
        String result = instance.getJobName();
        assertEquals(expResult, result);
    }

    /**
     * Test of isDefine static method, of class MarkLogicConfiguration.
     */
    @Test
    public void testIsDefined()
    {
        assertTrue(MarkLogicConfiguration.isDefined("TestTransformName"));
    }
    
    @Test
    public void testIsDefinedNull()
    {
        assertFalse(MarkLogicConfiguration.isDefined(null));
    }

    @Test
    public void testIsDefinedEmptyString()
    {
        assertFalse(MarkLogicConfiguration.isDefined(""));
    }

    @Test
    public void testIsDefinedBlankString()
    {
        assertFalse(MarkLogicConfiguration.isDefined(" ")); //In case a user enters a space/tab
    }

    @Test
    public void testIsDefinedNullString()
    {
        assertFalse(MarkLogicConfiguration.isDefined("null"));
    }

    @Test
    public void testIsDefinedNullCapString()
    {
        assertFalse(MarkLogicConfiguration.isDefined("NULL"));
    }

    /**
     * Test of createServerTransform method, of class MarkLogicConfiguration.
     */
    @Test
    public void testGenerateServerTransformWithoutName()
    {
        assertNull(instance.generateServerTransform(null, null));
    }

    @Test
    public void testGenerateServerTransformNameWithoutParams()
    {
        ServerTransform transform = instance.generateServerTransform("TestTransform", null);
        assertEquals("TestTransform", transform.getName());
        assertEquals(0, transform.size());
    }
    
    @Test
    public void testGenerateServerTransformConfigNameWithoutParams()
    {
        instance.setServerTransform("TestTransform");
        ServerTransform transform = instance.generateServerTransform(null, null);
        assertEquals("TestTransform", transform.getName());
        assertEquals(0, transform.size());
    }

    @Test
    public void testGenerateServerTransformNameAndConfigNameWithoutParams()
    {
        instance.setServerTransform("TestTransform-Not-Used");
        ServerTransform transform = instance.generateServerTransform("TestTransform", null);
        assertEquals("TestTransform", transform.getName());
        assertEquals(0, transform.size());
    }
    
    @Test
    public void testGenerateServerTransformWithEmptyParams()
    {
        ServerTransform transform = instance.generateServerTransform("TestTransform", "   ");
        assertEquals("TestTransform", transform.getName());
        assertEquals(0, transform.size());
    }

    @Test
    public void testGenerateServerTransformWithNullParams()
    {
        instance.setServerTransform("TestTransform");
        instance.setServerTransformParams("null");
        ServerTransform transform = instance.generateServerTransform(null, "not-used");
        assertEquals("TestTransform", transform.getName());
        assertEquals(0, transform.size());
    }

    @Test(expected = MarkLogicConnectorException.class)
    public void testGenerateServerTransformUnequalPairs()
    {
        createServerTransformTester("TestTransform", "entity-name,MyEntity,flow-name", true);
    }

    @Test(expected = MarkLogicConnectorException.class)
    public void testGenerateServerTransformUnequalPairs2()
    {
        createServerTransformTester("TestTransform", "entity-name,MyEntity,flow-name, ", false);
    }

    @Test
    public void testGenerateServerTransform()
    {
        createServerTransformTester("TestTransform", "entity-name,MyEntity,flow-name,loadMyEntity", true);
    }

    @Test
    public void testGenerateServerTransformWithSpaces()
    {
        createServerTransformTester("TestTransform", "entity-name, MyEntity, flow-name, loadMyEntity ", false);
    }

    private void createServerTransformTester(String name, String params, boolean useVars)
    {
        ServerTransform transform;
        if(useVars)
        {
            transform = instance.generateServerTransform(name, params);
        }
        else
        {
            instance.setServerTransform(name);
            instance.setServerTransformParams(params);
            
            transform = instance.generateServerTransform(null, null);
        }
        
        assertEquals(name, transform.getName());

        transformParamTester(transform, "entity-name", "MyEntity");
        transformParamTester(transform, "flow-name", "loadMyEntity");
    }

    private void transformParamTester(ServerTransform transform, String key, String value)
    {
        assertTrue(transform.containsKey(key));

        List<String> list = transform.get(key);
        assertEquals(1, list.size());
        assertEquals(value, list.get(0));
    }
}
