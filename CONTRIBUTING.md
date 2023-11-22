This guide describes how to develop and test the connector, which includes both running the project's JUnit tests and
manually testing the connector via Anypoint Studio. It also includes instructions for generating SonarQube code quality
reports.

## Initial Setup

To run any of the steps below, first verify that you have the following available;
[sdkman](https://sdkman.io/) is recommended for installing both of these:
* Java 8.x
* Maven 3.9.x

If you want to generate SonarQube code quality reports, you will also need Java 11 installed. 
Using sdkman will make it simply to install and switch between multiple versions of Java. 

[Docker Desktop](https://www.docker.com/products/docker-desktop/) is recommended for automating and simplifying the 
setup for developing and testing the connector. Without it, you can still deploy the test app to your local MarkLogic
instance and run the tests, but you will not be able to run SonarQube code quality reports.

If you are not using Docker, you can skip to the next section, the assumption being that you have a MarkLogic 
instance available for testing.

If you are able to use Docker, run the following:

    docker-compose up -d --build

This will create a MarkLogic service along with SonarQube and Postgres service. See the SonarQube section below for 
information on using SonarQube to generate code quality reports.

## Testing the documentation locally

See the section with the same name in the
[MarkLogic Koop contributing guide](https://github.com/koopjs/koop-provider-marklogic/blob/master/CONTRIBUTING.md).

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

### Run the tests

**Important** - to get munit working, we have to work around a bug in a Mule pom file. When you attempt to run 
`mvn clean package` below, you will most likely get an error about a missing Mule dependency. Here are the 
hopefully-temporary instructions to work around this issue:

1. Go to `~/.m2/repository/com/mulesoft/munit/2.3.11`.
2. Open `munit-2.3.11.pom`.
3. Change the `<mule.version>` element to have a value of "4.5.0-20220221" instead of "4.3.0-20220221".

This fix was found [in this Mule support page](https://help.mulesoft.com/s/question/0D52T000061exOfSAI/error-failed-to-execute-goal-commulesoftmunitmunitextensionsmavenplugin113test-defaulttest). 
We have no idea why fixing the pom for munit 2.3.11 works, given that our pom.xml is asking for munit 2.3.14. But it 
should allow for the below instructions to work. 

The project includes both JUnit tests and MUnit tests. The MUnit tests require that the connector is packaged and in the
target directory before running the tests. So you'll need to build first. To run the tests from the command-line, ensure
you are in the root directory of the project and use the following two Maven commands.
```
mvn clean package -DskipTests
mvn test
```

To run only the JUnit tests, you may use a switch to skip the MUnit tests.
```
mvn test -DskipMunitTests
```

** This switch is currently unavailable. ** 
To run only the Munit tests, you may use a switch to skip the JUnit tests.
```
mvn test -DskipSureFire=true
```

### Adding an MUnit test
To create a new MUnit test, a good place to start is with batch-read-write-test-suite.xml in src/test/munit. This is a
relatively simple test with some comments to explain things.

### Manually testing SSL

The connector supports 1-way and 2-way SSL with MarkLogic via Mule's `TLSContextFactory`, but we do not yet have 
automated tests for this due to the complexity of the test setup (we do have tests for an insecure trust manager, but
that is not a realistic approach in a production scenario). To manually verify this capability, please see 
the "Testing 2-way SSL with the Java Client" internal Wiki page. If you do not have access to this, you can achieve
a similar effect via the following steps:

1. Clone the [MarkLogic Java Client project](https://github.com/marklogic/java-client-api).
2. Follow the CONTRIBUTING.md instructions for getting the test application setup.
3. Open `TwoWaySSLTest` and put a debugger breakpoint in the start of the `teardown` method.
4. Run the debugger on one of the tests that does not test an error condition.

With the debugger having paused the program, the `java-unittest` MarkLogic app server will be in a state where it 
requires 2-way SSL. In addition, the test logging will identify the location of a Java keystore containing a private
key for authenticating with MarkLogic along with the public certificate matching the `java-unittest`'s certificate 
template. You can use that Java keystore for manually testing a Mule `TLSContextFactory`; the keystore will act as the
truststore as well. 

### Generating code quality reports with SonarQube

In order to use SonarQube, you must have used Docker to run this project's `docker-compose.yml` file and you must
have the services in that file running.

To configure the SonarQube service, perform the following steps:

1. Go to http://localhost:9000 .
2. Login as admin/admin. SonarQube will ask you to change this password; you can choose whatever you want ("password" works).
3. Click on "Create project manually".
4. Enter "marklogic-mule-connector" for the Project Name; use that as the Project Key too.
5. Enter "master" as the main branch name.
6. Click on "Next".
7. Click on "Use the global setting" and then "Create project".
8. On the "Analysis Method" page, click on "Locally".
9. In the "Provide a token" panel, click on "Generate". Copy the token to a safe place (although you can always generate a new one).

You now have a SonarQube project and a token to use for authentication. If you'd like, you can continue on the 
"Analyze your project" page in SonarQube to the "Run analysis" step, but this project's `pom.xml` file already has most 
of that configuration captured in it. You'll only need the token that you just generated for the following steps.

Because the connector needs to be built with Java 8, but SonarQube requires Java 11, you must first build and test the
code with Java 8 and then use Java 11 to run SonarQube. Annoying, yes, but there's not yet a way around this. 

So with Java 8, run `package` to build the project, and then run the tests. The tests produce the output that SonarQube
needs to generate its report:

    mvn clean package -DskipTests
    mvn test

After that completes, switch to Java 11 and run the following, using the token you obtained above:

    mvn sonar:sonar -Dsonar.token=your-token-pasted-here

When that completes, you will see a line like this near the end of the logging:

    [INFO] ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=marklogic-mule-connector

Click on that link. If it's the first time you've run the report, you'll see all issues. If you've run the report 
before, then SonarQube will show "New Code" by default. That's handy, as you can use that to quickly see any issues 
you've introduced on the feature branch you're working on. You can then click on "Overall Code" to see all issues. 


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

The flows expose endpoints at http://localhost:8081/export and http://localhost:8081/read . Each will query the 
`batch-input` collection in the `mule2-test-content` database. The former uses the "Export Documents" operation to 
read only the document contents, while the latter uses "Read Documents" and requires the use of a Mule batch job. Both
will write the 10 documents in the `batch-input` collection to different URIs and collections.

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
10. Add a MarkLogic write operation to the flow by choosing the MarkLogic group in the Mule
Palette and then dragging "Write Singledoc" into the flow, to the right of the third variable.
11. With the MarkLogic write operation selected, configure the MarkLogic search.
* Click the "+" beside Connector configuration
```
Host: localhost
Port: 8022
Authentication Type: DIGEST
Username: mule2-test-user
Passowrd: password
General->Config id: MarkLogic_Config
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

### Anypoint - MarkLogic Logging
To set the logging level for MarkLogic packages, update the appropriate log4j2.xml file with a line like the following.
Set the package name and add more copies of the line as necessary.
```
<AsyncLogger name="com.marklogic.mule.extension" level="DEBUG"/>
```