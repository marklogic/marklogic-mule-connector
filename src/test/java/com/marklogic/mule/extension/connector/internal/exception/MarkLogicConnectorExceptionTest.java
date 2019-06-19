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
package com.marklogic.mule.extension.connector.internal.exception;

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
        MarkLogicConnectorException exception = new MarkLogicConnectorException(errorMsg);

        assertEquals(errorMsg, exception.getMessage());
    }

    @Test
    public void testExceptionWithThrows()
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
