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
package com.marklogic.mule.extension.connector.internal.result.resultset;

import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jkrebs on 1/19/2020.
 */
public class MarkLogicXMLRecordExtractor extends MarkLogicRecordExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicXMLRecordExtractor.class);

    // Objects used for handling XML documents
    private StringHandle handle = new StringHandle();

    @Override
    protected Object extractRecord(DocumentRecord record) {
        StringHandle retVal = record.getContent(handle).withMimetype("application/xml").withFormat(Format.XML);
        return retVal.get();
    }
}