/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;

import java.io.InputStream;
import java.util.UUID;

public class WriteOperations {
    void writeDocuments(DatabaseClient databaseClient, InputStream content,
                        Format format, String permissions, int quality,
                        String collections, String uriPrefix, String uriSuffix, boolean generateUUID) {

        GenericDocumentManager documentManager = databaseClient.newDocumentManager();

        DocumentMetadataHandle documentMetadataHandle = new DocumentMetadataHandle().withQuality(quality);

        if (collections != null && !collections.isEmpty()) {
            String[] collectionArray = collections.split(",");
            documentMetadataHandle.withCollections(collectionArray);
        }

        if (permissions != null && !permissions.isEmpty()) {
            documentMetadataHandle.getPermissions().addFromDelimitedString(permissions);
        }
        StringBuilder uri = new StringBuilder();
        if (uriPrefix != null && !uriPrefix.isEmpty()) {
            uri.append(uriPrefix);
        }
        if (generateUUID) {
            uri.append(UUID.randomUUID());
        }
        if (uriSuffix != null && !uriSuffix.isEmpty()) {
            uri.append(uriSuffix);
        }
        documentManager
            .write(uri.toString(),
                documentMetadataHandle,
                new InputStreamHandle(content).withFormat(format));
    }
}
