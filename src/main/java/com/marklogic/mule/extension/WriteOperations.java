package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;

import java.io.InputStream;
import java.util.UUID;

public class WriteOperations{
    void writeDocuments(DatabaseClient databaseClient, InputStream content,
                        Format format, String permissions, int quality,
                        String collections, String uriPrefix, String uriSuffix, boolean generateUUID) {

        GenericDocumentManager documentManager = databaseClient.newDocumentManager();

        DocumentMetadataHandle documentMetadataHandle = new DocumentMetadataHandle().withQuality(quality);

        if(collections!=null && !collections.isEmpty()){
            String[] collectionArray = collections.split(",");
            documentMetadataHandle.withCollections(collectionArray);
        }

        if(permissions!=null && !permissions.isEmpty()){
            documentMetadataHandle.getPermissions().addFromDelimitedString(permissions);
        }
        StringBuilder uri = new StringBuilder();
        if(uriPrefix!= null && !uriPrefix.isEmpty()){
            uri.append(uriPrefix);
        }
        if(generateUUID){
            uri.append(UUID.randomUUID());
        }
        if(uriSuffix!=null && !uriSuffix.isEmpty()){
            uri.append(uriSuffix);
        }
        documentManager
            .write(uri.toString(),
                documentMetadataHandle,
                new InputStreamHandle(content).withFormat(format));
    }
}
