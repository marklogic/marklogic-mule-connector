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
import com.marklogic.client.io.BytesHandle;

/**
 * Created by jkrebs on 1/19/2020.
 */
public class MarkLogicBinaryRecordExtractor extends MarkLogicRecordExtractor {

    // Objects used for handling binary documents
    private BytesHandle binaryHandle = new BytesHandle();

    @Override
    protected Object extractRecord(DocumentRecord record) {
        return record.getContent(binaryHandle).get();
    }
}