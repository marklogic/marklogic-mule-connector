package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;

import java.io.InputStream;
import java.util.UUID;

public class WriteOperations{
    void writeDocuments(DatabaseClient databaseClient, InputStream content, String uri,
                        Format format, String permissions, int quality,
                        String collections) {

        GenericDocumentManager documentManager = databaseClient.newDocumentManager();

        DocumentMetadataHandle documentMetadataHandle = new DocumentMetadataHandle().withQuality(quality);

        if(collections!=null && !collections.isEmpty()){
            String[] collectionArray = collections.split(",");
            documentMetadataHandle.withCollections(collectionArray);
        }

        if(permissions!=null && !permissions.isEmpty()){
            documentMetadataHandle.getPermissions().addFromDelimitedString(permissions);
        }
        documentManager
            .write((uri!=null && !uri.isEmpty()) ?uri: UUID.randomUUID().toString(),
                documentMetadataHandle,
                new InputStreamHandle(content).withFormat(format));
    }
}
