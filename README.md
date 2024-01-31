# MarkLogic Connector for Mulesoft

The MarkLogic Connector for Mulesoft is a [reusable extension](https://docs.mulesoft.com/connectors/) for
integrating a Mule application with MarkLogic. The connector supports reading data from and writing data to MarkLogic.

The MarkLogic MuleSoft connector supports reading data from and writing data to MarkLogic
[in a Mule application](https://docs.mulesoft.com/connectors/introduction/introduction-to-anypoint-connectors).
MuleSoft flows can leverage the full power of [MarkLogic's search indexes](https://docs.marklogic.com/guide/concepts/indexing)
to retrieve documents and can efficiently write large quantities of any kind of data to MarkLogic's schema-agnostic databases.

The connector has the following system requirements:

* Mule 4.3 or higher.
* Anypoint Studio 7.x or higher.
* MarkLogic 9 or higher.

For more information, please see [the User Guide](https://marklogic.github.io/marklogic-mule-connector/) or 
[the installation guide](https://marklogic.github.io/marklogic-mule-connector/installation.html) to get started.

For users with experience with the 1.x version of this connector, please note that the 2.0.0 release has intentionally
combined previous operations related to querying for documents into a single
["Read documents"](https://marklogic.github.io/marklogic-mule-connector/reading.html)
operation that supports any type of MarkLogic search query. Deprecated operations have also been removed. Finally,
the "Delete docs" operation found in the 1.x connector is not yet supported due to the approach used in the 1.x
connector not being consistent with the guidelines for a Mule connector. If you find yourself in need of this operation,
please [submit an issue](https://github.com/marklogic/marklogic-mule-connector/issues) to describe your use case so we
can provide the optimal solution for it.