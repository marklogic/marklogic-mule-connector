# Mulesoft Connector Extension for MarkLogic

## About the Connector


The intent of this connector is to demonstrate a Mulesoft AnypointStudio (Mulesoft's Eclipse-based graphical IDE) Flow to ingest MySQL relational database content into MarkLogic as JSON documents, much like the MarkLogic NiFi Connector does. 

Similarly to the NiFi Connector, the Mulesoft Connector is predicated on the use of the MarkLogic Data Movement SDK to communicate to MarkLogic.  A ConnectionProvider class exists to manage the connections to MarkLogic.

This Connector demo is built on Mule 4.1 SDK and AnypointStudio 7.1.  The genesis of the demo Connector code comes from scaffolding from a Maven archetype:

```
mvn archetype:generate
  -DarchetypeCatalog=http://repository.mulesoft.org/releases/
  -DarchetypeGroupId=org.mule.extensions
  -DarchetypeArtifactId=mule-extensions-archetype
  -DarchetypeVersion=1.1.0
  -DgroupId=com.marklogic
  -DartifactId=marklogic-mule-connector
  -Dversion=0.0.1-SNAPSHOT
  -Dpackage=com.marklogic.mule.extension.connector
  -DextensionName=MarkLogicConnector
```


Add this dependency to your application pom.xml

```
<groupId>com.marklogic</groupId>
<artifactId>marklogic-mule-connector</artifactId>
<version>0.0.1</version>
```
