# Example project using the MarkLogic Connector for Mulesoft

This example Anypoint project demonstrates the use of the MarkLogic MuleSoft connector for integrating a Mule
application with MarkLogic. It is assumed that you have MarkLogic running on your localhost with all of the
out-of-the-box defaults available.

## Example Flows
The example has two flows.

The first flow reads files from your local filesystem and inserts them into the "Documents" database in
MarkLogic. The flow uses a Batch Job to make the flow asynchronous and to send the files to MarkLogic in
batches of 5 in order to be efficient. It includes the "from-disk" collection on each of the documents.

The second queries the "Documents" database for the documents written in the first flow. It does this by
specifying the "from-disk" collection in an otherwise empty query. Once those documents have been retrieved,
a Batch Job writes each document into the target directory.

## Running the example
In order to run this demonstration locally, you need to make a couple of minor changes to point the file
operations to the correct location on your local system.

1. Open the Anypoint Studio with the workspace of your choice.
2. Click File->Import.
3. Select "Anypoint Studio"->"Anypoint Studio project from File System" & click Next.
4. For Project Root, enter the path to your local copy of this project plus "/examples/demo-project" & click Finish.
The project will then be imported into your workspace.
5. In the project, open the "properties.yaml" file in the src/main/resources directory, and update the "path" property
to be the absolute path to your local version of this project.
6. Save your changes.
7. To view the flows, open the "demo" flow file (src/main/mule/demo.xml).
8. Right-click in the flow editing window and choose "Run project demo-project".
9. Wait for the project to finish initializing. You'll know it is ready when you see messages in the console that
say, "Message source 'listener' on flow 'Read_Documents' successfully started" and
"Message source 'listener' on flow 'Write_Documents' successfully started".
10. To run the Write_Documents flow, open a web browser and point it to: http://localhost:8100/writeDocuments.
11. Use Query Console (https://docs.marklogic.com/guide/qconsole) to explore the "Documents" database and verify that the
documents have been loaded. Query Console should be available at http://localhost:8000/qconsole/.
12. To run the "Read_Documents" flow, point your browser to http://localhost:8100/readDocuments.
13. Use a file browser to verify that the documents have been written to the /demos/demo-project/output/
    directory in your local project.