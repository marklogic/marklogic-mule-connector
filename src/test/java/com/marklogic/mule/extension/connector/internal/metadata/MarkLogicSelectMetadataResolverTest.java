/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
