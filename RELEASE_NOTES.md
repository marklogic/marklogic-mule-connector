# MarkLogic Connector - Mule 4

The MarkLogic Connector for Mulesoft is a [reusable extension](https://docs.mulesoft.com/connectors/) for integrating a
Mule application with MarkLogic. The connector supports reading data from and writing data to MarkLogic
[in a Mule application](https://docs.mulesoft.com/connectors/introduction/introduction-to-anypoint-connectors). MuleSoft flows can leverage the full power of [MarkLogic's search indexes](https://docs.marklogic.com/guide/concepts/indexing) to
retrieve documents and can efficiently write large quantities of any kind of data to MarkLogic's schema-agnostic databases.

## Version 2.0.0 - February 2024
### Version Compatibility
| Application | Version |
| ----------- |------|
| Mule Runtime | 4.3+ |
| Anypoint Studio | 7.0+ |
| MarkLogic | 9.0+ |

For more information, please see [the User Guide](https://marklogic.github.io/marklogic-mule-connector/) or
[the installation guide](https://marklogic.github.io/marklogic-mule-connector/installation.html) to get started.

## Migrating From Older Versions

For users with experience with the 1.x version of this connector, please note that the 2.0.0 release has intentionally
combined previous operations related to querying for documents into a single ["Read documents"](https://marklogic.github.io/marklogic-mule-connector/reading.html)
operation that supports any type of MarkLogic search query. Deprecated operations have also been removed. Finally,
the "Delete docs" operation found in the 1.x connector is not yet supported due to the approach used in the 1.x
connector not being consistent with the guidelines for a Mule connector. If you find yourself in need of this operation,
please [submit an issue](https://github.com/marklogic/marklogic-mule-connector/issues) to describe your use case so we
can provide the optimal solution for it.

## Features

* Read Documents - Search for and read documents, along with document metadata, from a MarkLogic database.
* Write Documents - Write payloads and attributes as documents in a MarkLogic database.

## About MarkLogic

MarkLogic is a database designed from the ground up to make massive quantities of heterogeneous data easily accessible
through search. The design philosophy behind the evolution of MarkLogic is that storing data is only part of the solution.
The data must also be quickly and easily retrieved and presented in a way that makes sense to different types of users.
Additionally, the data must be reliably maintained by an enterprise grade, scalable software solution that runs on commodity
hardware. The purpose of this guide is to describe the mechanisms in MarkLogic that are used to achieve these objectives.

MarkLogic fuses together database internals, search-style indexing, and application server behaviors into a unified
system. It uses XML and JSON documents as its data model, and stores the documents within a transactional repository. It
indexes the words and values from each of the loaded documents, as well as the document structure. And, because of its
unique Universal Index, MarkLogic does not require advance knowledge of the document structure and adherence to a
particular schema. Through its application server capabilities, it is programmable and extensible.
