/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marklogic.mule.extension.connector.internal.exception;

import com.marklogic.mule.extension.connector.internal.exception.MarkLogicConnectorException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jshingler
 */
public class MarkLogicConnectorExceptionTest
{
    
    @Test
    public void testException()
    {
        String errorMsg = "Test MarkLogic Connection Exception";
        String thrownMsg = "Thrown error message";
        Exception error = new RuntimeException(thrownMsg);
        MarkLogicConnectorException exception = new MarkLogicConnectorException(errorMsg, error);
        
        assertEquals(errorMsg, exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals(thrownMsg, exception.getCause().getMessage());
    }
    
}
