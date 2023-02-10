/**
 * MarkLogic Mule Connector
 * <p>
 * Copyright © 2021 MarkLogic Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * <p>
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.client.io.marker.DocumentMetadataReadHandle;
import com.marklogic.mule.extension.connector.internal.result.resultset.RecordExtractor;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class MarkLogicMimeTypeTestCase {

    private RecordExtractor recordExtractor = new RecordExtractor();

    @Test
    public void testXml() {
        Object obj = recordExtractor.extractRecord(new TestRecord(new StringHandle("<test/>"), "application/xml"));
        assertEquals("<test/>", obj);
    }

    @Test
    public void testJson() {
        Object obj = recordExtractor.extractRecord(new TestRecord(new JacksonHandle(
            new ObjectMapper().createObjectNode().put("hello", "world")), "application/json"));
        LinkedHashMap map = (LinkedHashMap) obj;
        assertEquals("world", map.get("hello"));
    }

    @Test
    public void testText() {
        Object obj = recordExtractor.extractRecord(new TestRecord(new StringHandle("any text"), "text/plain"));
        assertEquals("any text", obj);
    }

    @Test
    public void testBinary() {
        Object obj = recordExtractor.extractRecord(new TestRecord(new BytesHandle("any text".getBytes()), null));
        assertEquals("any text", new String((byte[]) obj));
    }
}

class TestRecord implements DocumentRecord {

    private AbstractReadHandle fakeContent;
    private String mimetype;

    public TestRecord(AbstractReadHandle fakeContent, String mimetype) {
        this.fakeContent = fakeContent;
        this.mimetype = mimetype;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public DocumentDescriptor getDescriptor() {
        return null;
    }

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public String getMimetype() {
        return mimetype;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public <T extends DocumentMetadataReadHandle> T getMetadata(T t) {
        return null;
    }

    @Override
    public <T> T getMetadataAs(Class<T> aClass) {
        return null;
    }

    @Override
    public <T extends AbstractReadHandle> T getContent(T t) {
        return (T) fakeContent;
    }

    @Override
    public <T> T getContentAs(Class<T> aClass) {
        return null;
    }
}