---
layout: default
title: Introduction
nav_order: 1
---

The MarkLogic MuleSoft connector supports reading data from and writing data to MarkLogic 
[in a Mule application](https://docs.mulesoft.com/connectors/introduction/introduction-to-anypoint-connectors). MuleSoft flows can leverage the full power of 
[MarkLogic's search indexes](https://docs.marklogic.com/guide/concepts/indexing) to retrieve documents and can
efficiently write large quantities of any kind of data to MarkLogic's schema-agnostic databases.

The connector has the following system requirements:

* Mule 4.3 or higher.
* Anypoint Studio 7.x or higher.
* MarkLogic 9 or higher.

Please see [the installation guide](./installation.md) to get started.

For users with experience with the 1.x version of this connector, please note that the 2.0.0 release has intentionally
combined previous operations related to querying for documents into a single ["Read documents"](./reading.md)
operation that supports any type of MarkLogic search query. Deprecated operations have also been removed. Finally, 
the "Delete docs" operation found in the 1.x connector is not yet supported due to the approach used in the 1.x 
connector not being consistent with the guidelines for a Mule connector. If you find yourself in need of this operation, 
please [submit an issue](https://github.com/marklogic/marklogic-mule-connector/issues) to describe your use case so we 
can provide the optimal solution for it. 
