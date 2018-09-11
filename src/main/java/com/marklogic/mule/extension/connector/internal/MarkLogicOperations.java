package com.marklogic.mule.extension.connector.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import java.util.*;

import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.marklogic.client.io.*;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.datamovement.JobReport;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.ServerTransform;
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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/* This class is a container for operations, every public method in this class will be taken as an extension operation. */
public class MarkLogicOperations
{

    private final Logger logger = LoggerFactory.getLogger(MarkLogicOperations.class);

    // Loading files into MarkLogic asynchronously InputStream docPayload
  @MediaType(value = APPLICATION_JSON, strict = true)
  public String importDocs(@Config MarkLogicConfiguration configuration, @Connection MarkLogicConnection connection, InputStream docPayloads, String basenameUri) {

        System.out.println("uri list: " + basenameUri);

        // Parse the string representation of the list of base uris into a List of base uri strings
        List<String> uris = parseUriListString(basenameUri);

        DocumentMetadataHandle metah = new DocumentMetadataHandle();

        // Collections, quality, and permissions.
        // Permissions are additive to the rest-reader,read and rest-writer,update.
        String[] configCollections = configuration.getOutputCollections();
        if (!configCollections[0].equals("null")) {
            metah.withCollections(configCollections);
        }
        metah.setQuality(configuration.getOutputQuality());
        String[] permissions = configuration.getOutputPermissions();
        for (int i = 0; i < permissions.length - 1; i++) {
            String role = permissions[i];
            String capability = permissions[i + 1];
            switch(capability.toLowerCase()) {
                case "read" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.READ);
                    break;
                case "insert" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.INSERT);
                    break;
                case "update" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.UPDATE);
                    break;
                case "execute" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.EXECUTE);
                    break;
                case "node_update" :
                    metah.getPermissions().add(role, DocumentMetadataHandle.Capability.NODE_UPDATE);
                    break;
                default :
                    System.out.println("No additive permissions assigned");
            }
        }

        // Parse the given InputStream into a List of documents
        List documents = parseObjectStream(docPayloads);
        DatabaseClient myClient = connection.getClient();
        DataMovementManager dmm = myClient.newDataMovementManager();
        WriteBatcher batcher = dmm.newWriteBatcher();
        batcher.withBatchSize(configuration.getBatchSize())
        .withThreadCount(configuration.getThreadCount())
        .onBatchSuccess(batch-> {
            String successMsg = batch.getTimestamp().getTime() + " documents written: " + batch.getJobWritesSoFar(); 
            System.out.println(successMsg);
        })
        .onBatchFailure((batch,throwable) -> {
            throwable.printStackTrace();
        });
        
        String configTransform = configuration.getServerTransform();
        if ((configTransform == null) || (configTransform.equals("null"))) {
            System.out.println("Ingesting doc payload without a transform");
        } else {
            ServerTransform thistransform = new ServerTransform(configTransform);
            String[] configTransformParams = configuration.getServerTransformParams();
            if (!configTransformParams[0].equals("null") && configTransformParams.length % 2 == 0) {
                for (int i = 0; i < configTransformParams.length - 1; i++) {
                    String paramName = configTransformParams[i];
                    String paramValue = configTransformParams[i + 1];
                    thistransform.addParameter(paramName, paramValue);
                }            
            }
            batcher.withTransform(thistransform);
            System.out.println("Transforming input doc payload with transform: " + thistransform.getName());
        }

        // Create a template into which to put the base URI
        String uriTemplate = configuration.getOutputPrefix() + "%s" + configuration.getOutputSuffix();

        // start the job and feed input to the batcher
        JobTicket jt = dmm.startJob(batcher);
        try {
            for (int i = 0; i < documents.size(); i++) {
                // determine output URI for the current document
                String baseURI;
                String outURI;
                // If the config tells us to generate a new URI, just do that
                if (configuration.getGenerateOutputUriBasename() == Boolean.TRUE) {
                    baseURI = UUID.randomUUID().toString();
                } else {
                    // If we've moved past the end of the URI array, generate a new one
                    if (i >= uris.size()) {
                        baseURI = UUID.randomUUID().toString();
                    } else {
                        // Use the base URI, unless it's null or "",  then generate one
                        baseURI = uris.get(i);
                        if ((baseURI == null) || (baseURI.length() < 1)) {
                            baseURI = UUID.randomUUID().toString();
                        }
                    }
                }
                outURI = String.format(uriTemplate,baseURI);
                System.out.println("outURI: " + outURI);
                batcher.addAs(outURI,metah,documents.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start any partial batches waiting for more input, then wait
        // for all batches to complete. This call will block.
        batcher.flushAndWait();
        JobReport jr = dmm.getJobReport(jt);
        ObjectNode objectNode = createJsonJobReport(jr);
        dmm.stopJob(batcher);
        return objectNode.toString();
  }
  
  private ObjectNode createJsonJobReport(JobReport jr) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = mapper.createObjectNode();
        long successBatches = jr.getSuccessBatchesCount();
        long successEvents = jr.getSuccessEventsCount();
        long failBatches = jr.getFailureBatchesCount();
        long failEvents = jr.getFailureEventsCount();
        if (failEvents > 0) {
            obj.put("jobOutcome", "failed");
        } else {
            obj.put("jobOutcome", "successful");
        }
        obj.put("successfulBatches", successBatches);
        obj.put("successfulEvents", successEvents);
        obj.put("failedBatches", failBatches);
        obj.put("failedEvents", failEvents);
        System.out.println(obj.toString());
        return obj;
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
    /**
     *
     * @param uriListString : a list of URIs surrounded by [] and separated by commas
     * @return
     */
    private List<String> parseUriListString(String uriListString) {
        List<String> retVal;
        // If the uri list string is null or "", just return a new (empty) List
        if ((uriListString == null) || (uriListString.length() == 0)) {
            retVal = new ArrayList<String>();
        } else {
            // trim the [ and ] off the list string
            String trimmedList = uriListString.substring(1, uriListString.length() - 1);
            // parse the string by commas
            StringTokenizer parser = new StringTokenizer(trimmedList, ",");
            // count the number of items in the list
            int listLength = parser.countTokens();
            retVal = new ArrayList<String>(listLength);
            // if the list has no items, that means there are no commas. just add that to the list and bail.
            if (listLength == 0) {
                retVal.add(trimmedList.trim());
            } else {
                // otherwise, walk thru all the items and add them to the list
                while (parser.hasMoreElements()) {
                    retVal.add(parser.nextToken().trim());
                }
            }
        }
        return retVal;
    }
    private List parseObjectStream(InputStream stream) {
        List documents = new ArrayList();

        try {
            // Create an ObjectMapper/parser to pull JSON objects out of the given stream
            ObjectMapper mapper = new ObjectMapper();
            JsonParser parser = mapper.getFactory().createParser(stream);

            // If we've gotten this far without an exception, it means we have JSON
            System.out.println("Treating stream as json");

            // Make sure the first token is the start of a JSON array.
            JsonToken firstToken = parser.nextToken();

            if (firstToken != JsonToken.START_ARRAY && firstToken != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected an array or object, but got: " + firstToken);
            }
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                ObjectNode node = mapper.readTree(parser);
                documents.add(node.toString());
                System.out.println("adding: " + node.toString());
            }
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                // read everything from this START_OBJECT to the matching END_OBJECT
                // and return it as a tree model ObjectNode
                ObjectNode node = mapper.readTree(parser);
                documents.add(node.toString());
                System.out.println("adding: " + node.toString());
            }

            parser.close();

        } catch (JsonProcessingException e) {
            try {
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                saxParser.parse(stream,new DefaultHandler());
                System.out.println("Treating content as XML");
            } catch (SAXException e1) {
                System.out.println("Treating content as Binary");
                documents = new ArrayList();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }
    /**
     * Unit tests
     * @param args sent in on the command line
     */
    public static void main(String[] args) {

        MarkLogicOperations ops = new MarkLogicOperations();
        System.out.println("parseUriListString TEST 1.  Results should show 5 numbers with no spaces");
        String uriListString = "[10004, 10002, 10003, 10001, 10005]";
        for (String uri : ops.parseUriListString(uriListString))
            System.out.println("uri: |" + uri + "|");
        System.out.println("parseUriListString TEST 2.  Results should show 1 number with no spaces");
        uriListString = "[10004]";
        for (String uri : ops.parseUriListString(uriListString))
            System.out.println("uri: |" + uri + "|");
        System.out.println("parseUriListString TEST 3.  Shouldn't return anything");
        uriListString = "";
        for (String uri : ops.parseUriListString(uriListString))
            System.out.println("uri: |" + uri + "|");
        System.out.println("parseUriListString TEST 4.  Shouldn't return anything");
        uriListString = null;
        for (String uri : ops.parseUriListString(uriListString))
            System.out.println("uri: |" + uri + "|");
        String json = "[\n" +
                "  {\n" +
                "    \"employee\": {\n" +
                "      \"employeeNumber\": \"10004\",\n" +
                "      \"hireDate\": \"1986-12-01T00:00:00\",\n" +
                "      \"firstName\": \"Chirstian\",\n" +
                "      \"lastName\": \"Koblick\",\n" +
                "      \"birthDate\": \"1954-05-01T00:00:00\",\n" +
                "      \"gender\": \"M\",\n" +
                "      \"extractedDateTime\": \"2018-09-11T10:45:03.232-04:00\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"employee\": {\n" +
                "      \"employeeNumber\": \"10005\",\n" +
                "      \"hireDate\": \"1989-09-12T00:00:00\",\n" +
                "      \"firstName\": \"Kyoichi\",\n" +
                "      \"lastName\": \"Maliniak\",\n" +
                "      \"birthDate\": \"1955-01-21T00:00:00\",\n" +
                "      \"gender\": \"M\",\n" +
                "      \"extractedDateTime\": \"2018-09-11T10:45:03.232-04:00\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"employee\": {\n" +
                "      \"employeeNumber\": \"10003\",\n" +
                "      \"hireDate\": \"1986-08-28T00:00:00\",\n" +
                "      \"firstName\": \"Parto\",\n" +
                "      \"lastName\": \"Bamford\",\n" +
                "      \"birthDate\": \"1959-12-03T00:00:00\",\n" +
                "      \"gender\": \"M\",\n" +
                "      \"extractedDateTime\": \"2018-09-11T10:45:03.232-04:00\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"employee\": {\n" +
                "      \"employeeNumber\": \"10002\",\n" +
                "      \"hireDate\": \"1985-11-21T00:00:00\",\n" +
                "      \"firstName\": \"Bezalel\",\n" +
                "      \"lastName\": \"Simmel\",\n" +
                "      \"birthDate\": \"1964-06-02T00:00:00\",\n" +
                "      \"gender\": \"F\",\n" +
                "      \"extractedDateTime\": \"2018-09-11T10:45:03.232-04:00\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"employee\": {\n" +
                "      \"employeeNumber\": \"10001\",\n" +
                "      \"hireDate\": \"1986-06-26T00:00:00\",\n" +
                "      \"firstName\": \"Georgi\",\n" +
                "      \"lastName\": \"Facello\",\n" +
                "      \"birthDate\": \"1953-09-02T00:00:00\",\n" +
                "      \"gender\": \"M\",\n" +
                "      \"extractedDateTime\": \"2018-09-11T10:45:03.232-04:00\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
        // This should return a List of 5 employees
        System.out.println(ops.parseObjectStream(new ByteArrayInputStream(Charset.forName("UTF-16").encode(json).array())));
        String xml = "";
    }

}
