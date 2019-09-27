# MuleSoft Connector for MarkLogic #

-------------------------

## Installing the Connector ##
-------------------------

### Get the Connector directly from Anypoint Exchange! ###

* <a href="https://www.anypoint.mulesoft.com/exchange/com.marklogic/marklogic-mule-connector/">The latest version (1.1.0) is available directly from Anypoint Exchange</a>.  

* Both Anypoint Studio and Anypoint Design Center offer ease of access to the Anypoint Exchange for direct import of the Connector for use in your flows.

If you wish to install from the source code in this repository, see "<a href="wiki/Building-and-Scaffolding-the-Connector">Building and Scaffolding the Connector</a>" on the Wiki.

### Learn More ###
-------------------------

To get started, visit the <a href="https://github.com/marklogic-community/marklogic-mule-connector/wiki">Wiki</a>.


### About the Connector ###
------------------------

<a href="http://marklogic.com">MarkLogic</a> is a NoSQL database designed for speed and scale, without sacrificing the enterprise features required to run mission-critical, operational applications. Using a multi-model approach, MarkLogic provides unprecedented flexibility to integrate and store all of your most critical data, and then view that data as documents, as a graph, or as relational data. You can avoid expensive and brittle ETL and better manage the entities and relationships with which your business works.

<a href="http://mulesoft.com">MuleSoft</a> is a enterprise-grade, lightweight Java <a href="https://www.mulesoft.com/resources/esb-integration">Enterprise Service Bus and Application Orchestration platform</a> that encourages an API-first approach to services, workflows, and data integration. In fact, one can even define and serve RAML-based REST APIs within Mulesoft to orchestrate data movement between flow components.

The project provides the MuleSoft community with a MarkLogic Connector (via the <a href="https://www.mulesoft.com/exchange/">Anypoint Exchange</a>). It delivers data movement and transformation capabilities for use within MuleSoft <a href="https://www.mulesoft.com/platform/studio">Anypoint Studio</a> and <a href="https://anypoint.mulesoft.com/designcenter/">Anypoint Design Center</a> flows.  Anypoint Studio  and Anypoint Design Center, respectively, are MuleSoft's <a href="https://www.eclipse.org/">Eclipse</a>- and web-based graphical IDEs <a href="https://www.mulesoft.com/platform/api/flow-designer-integration-tool">used to design, author, and run flows</a>. 

Despite the graphical design nature of flows, under the covers, each flow in Mule is simply an XML file defining the workflow.  If one chooses, flow authoring can be done entirely in XML, especially within Anypoint Studio. See the flow XMLs within the <a href="examples/">collection of examples</a> to learn more.

The MuleSoft Connector for MarkLogic is built atop the <a href="https://github.com/marklogic/java-client-api">MarkLogic Java Client API</a> and <a href="https://docs.marklogic.com/guide/java/data-movement">Data Movement SDK (DMSDK)</a>, which are used to do all the work with MarkLogic. The Connector is based on the <a href="https://www.mulesoft.com/lp/dl/mule-esb-enterprise">Mule 4 Runtime Engine</a>, <a href="https://docs.mulesoft.com/mule-sdk/1.1/">Mule SDK 1.1</a>, and is developed and tested with Anypoint Studio 7.
