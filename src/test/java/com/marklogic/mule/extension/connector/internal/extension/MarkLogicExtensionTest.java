/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.marklogic.mule.extension.connector.internal.extension;

import com.marklogic.mule.extension.connector.internal.extension.MarkLogicExtension;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jshingler
 */
public class MarkLogicExtensionTest
{
    
    @Test
    public void testExtensionCreation()
    {
        assertNotNull(new MarkLogicExtension());
    }
    
}
