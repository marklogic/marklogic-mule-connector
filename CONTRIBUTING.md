## Connector Testing
Tests are an important part of the development process for this connector. For automated testing,
we use Java/JUnit. Instructions for running those tests are just below. However, it is
also frequently useful to use Anypoint Studio to create sample flows for trying out the
connector in the same way that users will be using the connector. Instructions for using
Anypoint Studio are also including a little further down.

### Deploy the test application
Both the automated Java tests as well as the Anypoint Studio examples rely on the included test application.
So, the first step is to deploy that application. Follow these steps to deploy the test application to your local MarkLogic database:
1. Clone this repository and navigate to the root directory on the command line.
2. Navigate to the test-app directory, `cd test-app`
3. Create a file named `gradle-local.properties` and add `mlPassword=value`, substituting "value" for the password of your MarkLogic admin user.
4. Run: `./gradlew -i mlDeploy` to deploy the test application to MarkLogic.
5. Use `cd ..` to return to the root directory of the repository.

### Environment Requirements
Verify that you have the required tools installed.
* Java 8 or higher
* Maven 3.x

### Run the Java tests
To run the tests from the command-line, ensure you are in the root directory of the project
and use the Maven command with the "test" goal.
```
mvn clean test
```

### Using Anypoint Studio
You can also use Anypoint Studio for testing flows using this connector. Follow either
of the following sets of steps (Quickstart or Step-By-Step) to set up an example flow
using this connector, the MarkLogic database, and Anypoint Studio from MuleSoft.

To get started with Anypoint Studio, download and install the latest Anypoint Studio and
Mule from https://www.mulesoft.com/lp/dl/anypoint-mule-studio


### Publishing the connector locally
In order for the Mule connector to be used in a Mule environment such as Anypoint Studio,
it must be published to your local Maven repository. Then a build tool such as Maven or
Gradle will be able to find the connector and include it in a project. 

* Since "install" is the Maven goal for publishing to your local Maven repository, run
this command to build and install the connector to your local Maven repository:
```
mvn clean install -DskipTests
```

### Anypoint - Quickstart
This method uses a complete project created to work with the test-app and requires only
a few steps to get up and running.
1. Start Anypoint Studio either with an existing workspace or create a new workspace.
2. Choose File->Import...
3. Choose Anypoint Studio -> Anypoint Studio project from File System, and click Next
4. For Project Root, select the src/test/studio-workspace/marklogicQuickstart directory in this project, and click Finish
5. Click the Green "Run" button in the toolbar.
6. Once running, the scheduler will cause the flow to run every 10 seconds. Every time
the flow runs, it will insert a document in the database. The document uris are based on
random guids (<guid>.json) and contain a single timestamp property. Use QConsole to verify.

### Anypoint - Step-By-Step
This method provides a step-by-step guide to creating a new workspace, a new project, and adding a flow.
1. In an AS workspace of your choosing, use the menu to create a new mule project by
selecting File->New->Mule Project. In the popup window, give the project a name and click "Finish".
2. Add this connector to the project as a Maven dependency by right-clicking on the
project name, then choose Mule->Add Maven Dependency
3. In the popup, paste the following in the text area on the right and click "Finish".
```			
<dependency>
    <groupId>com.marklogic</groupId>
    <artifactId>marklogic-mule-connector</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <classifier>mule-plugin</classifier>
</dependency>
```
4. Open the default mule flow by double-clicking on the generated XML file under "src/main/mule".
5. Add an HTTP listener by choosing the HTTP group in the Mule Palette and then dragging "Listener" into the Message Flow work area.
6. With the new listener selected, configure the listener
* Add Connector configuration - click the "+".
  * The defaults are generally fine
  * Click OK
* Set Listener->General->General->Path to "/stepByStep"
7. Set a variable in the flow by choosing the Core group in the Mule Palette and then
dragging "Set Variable" into the flow, to the right of the listener. Then update the
settings for the operation:
* Name: name
* Value:
```
#[ %dw 2.0
output application/json
---
attributes.queryParams.name default '' ]
```
8. Set another variable in the flow by choosing the Core group in the Mule Palette and
then dragging "Set Variable" into the flow, to the right of the first variable.
Then update the settings for the operation:
* Name: docContent
* Value:
```
#[ "{ \"Timestamp\": \"" ++ now() ++ "\", \"name\": \"" ++ vars.name ++ "\" }" ]
```
9. Set a third variable in the flow by choosing the Core group in the Mule Palette and
then dragging "Set Variable" into the flow, to the right of the second variable. Then
update the settings for the operation:
* Name: uri
* Value:
```
#[ "/" ++ uuid() ++ ".json" ]
```
10. Add a MarkLogic write operation to the flow by choosing the Basic group in the Mule
Palette and then dragging "Write Singledoc" into the flow, to the right of the third variable.
11. With the MarkLogic write operation selected, configure the MarkLogic search.
* Click the "+" beside Connector configuration
```
Host: localhost
Port: 8022
Authentication Type: DIGEST
Username: mule2-test-user
Passowrd: password
General->Config id: Basic_Config
```
* In the General area of the General config tab, set Content to #[vars.docContent]
* In the General area of the General config tab, set Uri to #[vars.uri]
12. Add a set-payload operation to the flow by choosing the Core group in the Mule Palette and then dragging "Set Payload" into the flow, to the right of the MarkLogic write operation. Then update the settings for the operation:
* Value: #[ vars.docContent ]
13. Save the project and click the Green "Run" button.
14. Once running, you can run the flow by pointing a browser to http://localhost:8081/stepByStep. The flow will create a document in the database with a random UUID in the uri and the web response shown in your browser will be the contents of that document. It will looks something like:
```
{"Timestamp":"2023-10-16T21:04:20.973-04:00", "name":""}
```
15. You may also pass a "name" query parameter by pointing your browser to http://localhost:8081/stepByStep?name=Bartholemew. That will create a document in the database that will look something like:
```
{"Timestamp":"2023-10-16T21:17:14.623-04:00", "name":"Bartholemew"}
```

### Anypoint - Debugging
Steps to debug a flow
1. Change the Anypoint Studio perspeective to the Mule Debug perspective using the top-level menus. Choose Window -> Perspective -> Open Perspective -> Mule Debug. Alternatively, click the "Mule Debug" icon in the top-right corner of the studio.
2. Run the application in debug mode using the top-level menus. Choose Run -> Debug As -> Mule Application. Alternatively, click the "Debug" icon in the toolbar.
3. In the "Message Flow" tab, right-click an operation and choose "Add breakpoint" to stop execution on that operation.
4. Initiate the flow.
At this point you'll be able to examine variables and create/view watches.

### Anypoint - Logging
Effective logging can be tricky depending on the payload. However, to log the payload
after a MarkLogic connector operation, add a logger element after the operation element.
```
<logger message="#[payload.typedValue]" level="INFO" doc:name="Logger"/>
```
