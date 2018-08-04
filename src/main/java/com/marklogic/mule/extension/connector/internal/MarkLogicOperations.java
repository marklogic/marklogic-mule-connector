package com.marklogic.mule.extension.connector.internal;

import java.util.UUID;

import com.marklogic.client.io.*;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.ext.datamovement.job.AbstractQueryBatcherJob;
import com.marklogic.client.ext.datamovement.job.ExportToFileJob;
import com.marklogic.client.ext.datamovement.job.SimpleQueryBatcherJob;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations
{

    private final Logger logger = LoggerFactory.getLogger(MarkLogicOperations.class);

    // Loading files into MarkLogic asynchronously byte[] docPayload
    @MediaType(value = ANY, strict = false)
    public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection, String docPayload, String basenameUri)
    {
        String payload = docPayload;
        System.out.println(payload);
        DocumentMetadataHandle metah = new DocumentMetadataHandle();

        // Collections, quality, and permissions.  
        // Permissions are additive to the rest-reader,read and rest-writer,update.
        metah.withCollections(configuration.getOutputCollections());
        metah.setQuality(configuration.getOutputQuality());
        String[] permissions = configuration.getOutputPermissions();
        for (int i = 0; i < permissions.length - 1; i++)
        {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch (capability.toLowerCase())
            {
                case "read":
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert":
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update":
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute":
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update":
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default:
                    System.out.println("No additive permissions assigned");
            }
        }

        // determine output URI
        String outURI;
        if (basenameUri.length() > 0 && configuration.getGenerateOutputUriBasename() == Boolean.FALSE)
        {
            outURI = configuration.getOutputPrefix() + basenameUri + configuration.getOutputSuffix();
        }
        else
        {
            String uuid = UUID.randomUUID().toString();
            outURI = configuration.getOutputPrefix() + uuid + configuration.getOutputSuffix();
        }

        // create and configure the job
        DatabaseClient myClient = connection.getClient();
        DataMovementManager dmm = myClient.newDataMovementManager();
        WriteBatcher batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(configuration.getBatchSize())
                .withThreadCount(configuration.getThreadCount())
                .onBatchSuccess(batch ->
                {
                    System.out.println(batch.getTimestamp().getTime() + " documents written: " + batch.getJobWritesSoFar());
                })
                .onBatchFailure((batch, throwable) ->
                {
                    throwable.printStackTrace();
                });

        // start the job and feed input to the batcher
        dmm.startJob(batcher);
        try
        {
            batcher.add(outURI, metah, new StringHandle(payload));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Start any partial batches waiting for more input, then wait
        // for all batches to complete. This call will block.
        batcher.flushAndWait();
        dmm.stopJob(batcher);
        return "Success";
    }

    /* Example of an operation that uses the configuration and a connection instance to perform some action. */
    @MediaType(value = ANY, strict = false)
    public String retrieveInfo(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection)
    {
        return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
    }

    //TODO: Make toString function for UI
    public enum WhereMethod
    {
        Collections,
        Uris,
        UriPattern,
        UrisQuery
    }

    //TODO: Make generic for different file types
    //Move to its own file
    private class DocumentExportListener extends ExportListener
    {
        private List<String> documents = new ArrayList<>();

        public DocumentExportListener()
        {
            super();
            this.onDocumentReady(documentRecord ->
            {
                documents.add(documentRecord.getContent(new StringHandle()).get());
            });
        }
        
        public List<String> getDocuments()
        {
            return documents;
        }
        
    }
    
    //TODO: Make generic for different file types
    @MediaType(value = ANY, strict = false)
    public List<String> fetchFile(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection,
            WhereMethod whereMethod, String where)
    {
        //Change to logger.info
        System.out.printf("Fetching File using %s where %s\n", whereMethod, where);

        DocumentExportListener exportListener = new DocumentExportListener();
        
        SimpleQueryBatcherJob job = new SimpleQueryBatcherJob();

        job.addUrisReadyListener(exportListener);
        job.setAwaitCompletion(true);
        job.setStopJobAfterCompletion(true);

        whereFile(configuration, connection, whereMethod, where, job, new Properties());
        
        return exportListener.getDocuments();
    }

    @MediaType(value = ANY, strict = false)
    public String exportFile(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection,
            WhereMethod whereMethod, String where, String exportFile,
            @Optional(defaultValue = "") String fileHeader, @Optional(defaultValue = "") String fileFooter,
            @Optional(defaultValue = "") String recordPrefix, @Optional(defaultValue = "") String recordSuffix)
    {
        //Change to logger.info
        System.out.printf("Exporting File using %s where %s\n", whereMethod, where);

        Properties props = new Properties();

        props.setProperty("exportPath", exportFile);

        props.setProperty("fileHeader", fileHeader);
        props.setProperty("fileFooter", fileFooter);
        props.setProperty("recordPrefix", recordPrefix);
        props.setProperty("recordSuffix", recordSuffix);
        //TBD: Add property
//      props.setProperty("transform", );

        return whereFile(configuration, connection, whereMethod, where, new ExportToFileJob(), props);
    }

    //TODO: Change return to type to a Generic type
    private String whereFile(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection,
            WhereMethod whereMethod, String where,
            AbstractQueryBatcherJob job, Properties props)
    {
        //FYI: Built into the AbstractQueryBatcherJob you can "add" all of the where properties
        //     It "priorities" the where selection in order to: 
        //        whereUris, whereCollection, whereUriPattern and lastly whereUrisQuery
        switch (whereMethod)
        {
            case Collections:
                props.setProperty("whereCollections", where);
                break;
            case Uris:
                props.setProperty("whereUris", where);
                break;
            case UriPattern:
                props.setProperty("whereUriPattern", where);
                break;
            case UrisQuery:
                throw new UnsupportedOperationException("Currently URIs Query is not supported");
        }

        return queryFile(configuration, connection, job, props);
    }

    //FIXME: Need to figure out how Mule works.
    //       Probably best to thow an exception!
    private String queryFile(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection,
            AbstractQueryBatcherJob job, Properties props)
    {
        props.setProperty("batchSize", configuration.getBatchSize() + "");
        props.setProperty("threadCount", configuration.getThreadCount() + "");
        //TBD: Add property
//      props.setProperty("consistentSnapshot", );
//      props.setProperty("jobName", );
//      props.setProperty("logBatches", );
//      props.setProperty("logBatchesWithLogger", );

        //TODO: Log properties?? 
        System.out.printf("\t - Properties: %s\n", props);

        List<String> issues = job.configureJob(props);

        if (issues.size() > 0)
        {
            //TODO: Log and return
            return issues.stream().collect(Collectors.joining("\n"));
        }
        else
        {
            job.run(connection.getClient());
            //TODO: Figure out if result of "run" was actually successful 
            return "Success";
        }
    }

}
