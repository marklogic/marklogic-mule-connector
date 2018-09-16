package com.marklogic.mule.extension.connector.internal;

import java.io.InputStream;

import java.util.*;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.io.*;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.ext.datamovement.job.AbstractQueryBatcherJob;
import com.marklogic.client.ext.datamovement.job.ExportToFileJob;
import com.marklogic.client.ext.datamovement.job.SimpleQueryBatcherJob;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

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
    private static final String OUTPUT_URI_TEMPLATE = "%s%s%s"; // URI Prefix + basenameUri + URI Suffix

    // Loading files into MarkLogic asynchronously InputStream docPayload
  @MediaType(value = APPLICATION_JSON, strict = true)
  public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection, InputStream docPayloads, String basenameUri) {

        // Get a handle to the Insertion batch manager
        MarkLogicInsertionBatcher batcher = MarkLogicInsertionBatcher.getInstance(configuration,connection);

        // Determine output URI
        // If the config tells us to generate a new UUID, do that
        if (configuration.getGenerateOutputUriBasename() == Boolean.TRUE) {
            basenameUri = UUID.randomUUID().toString();
        // Also, if the basenameURI is blank for whatever reason, use a new UUID
        } else if ((basenameUri == null) || (basenameUri.length() < 1)) {
            basenameUri = UUID.randomUUID().toString();
        }

        // Assemble the output URI components
        String outURI = String.format(OUTPUT_URI_TEMPLATE, configuration.getOutputPrefix(),basenameUri,configuration.getOutputSuffix());

        // Actually do the insert and return the result
        return batcher.doInsert(outURI,docPayloads);
  }
  @MediaType(value = APPLICATION_JSON, strict = true)
  public String getJobReport(String jobID) {
      ObjectNode obj = new ObjectMapper().createObjectNode();
      obj.put("jobID", jobID); 
      String result = obj.toString();
      MarkLogicInsertionBatcher insertionBatcher;
      try {
          insertionBatcher = MarkLogicInsertionBatcher.getInstance();
          if (insertionBatcher.jobIDMatches(jobID)) {
              result = insertionBatcher.createJsonJobReport(jobID);
          }
      } catch (java.lang.IllegalStateException e) {
          // MarkLogicInsertionBatcher has not been instantiated, therefore it has no results to return.  Could be that
          // the jobID refers to a query job
      }

      // Add support for query result report here!
      return result;

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
