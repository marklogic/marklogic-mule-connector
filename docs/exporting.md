---
layout: default
title: Exporting Documents
nav_order: 6
---

The MarkLogic MuleSoft connector provides an "Export Documents" operation for reading batches of documents from MarkLogic via
[MarkLogic's powerful search engine](https://docs.marklogic.com/guide/concepts/search). The connector leverages
[Mule's streaming support](https://docs.mulesoft.com/mule-runtime/latest/streaming-about) to allow for large
numbers of documents to be efficiently retrieved from MarkLogic. 

The "Export Documents" operation is identical to the ["Reading Documents" operation](./reading.md) with one distinction 
- the export operation only returns document contents and will not include URIs or document metadata. This operation is
typically appropriate when exporting data from MarkLogic to another system where you do not have any use for 
document URIs. 