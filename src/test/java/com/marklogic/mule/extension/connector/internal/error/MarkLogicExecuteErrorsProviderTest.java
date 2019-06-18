/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marklogic.mule.extension.connector.internal.error;

import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

/**
 *
 * @author jshingle
 */
public class MarkLogicExecuteErrorsProviderTest
{
    
    /**
     * Test of getErrorTypes method, of class MarkLogicExecuteErrorsProvider.
     */
    @Test
    public void testGetErrorTypes()
    {
        MarkLogicExecuteErrorsProvider instance = new MarkLogicExecuteErrorsProvider();
        Set<ErrorTypeDefinition> result = instance.getErrorTypes();
        
        assertEquals(1, result.size());
        assertTrue(result.contains(MarkLogicConnectorSimpleError.DATA_MOVEMENT_ERROR));
    }
    
}
