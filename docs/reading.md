---
layout: default
title: Reading Documents
nav_order: 4
---

The MarkLogic MuleSoft connector provides a "Read Documents" operation for reading batches of documents and their 
metadata from MarkLogic via 
[MarkLogic's powerful search engine](https://docs.marklogic.com/guide/concepts/search). The connector leverages
[Mule's streaming support](https://docs.mulesoft.com/mule-runtime/latest/streaming-about) to allow for large 
numbers of documents to be efficiently retrieved from MarkLogic. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Overview

The "Read Documents" operation can be added to a flow in Anypoint Studio from the "MarkLogic" module in the Mule Palette. 
The operation provides a number of fields and dropdowns for configuring which documents are read and what is returned
for each document. Upon clicking into each text field, Anypoint will display a small information icon to the left of 
the field. You can hover over this to view a description of the text field. For dropdown fields, you can hover over 
the dropdown to view a description of the field. The sections below provide additional information on how to use
this operation.

## Specifying a query

The "Read Documents" operation supports several different approaches for reading documents. You must select 
one of these approaches, each of which is described in the sections below.

### Querying by document URIs

The "Document URIs" field allows you to specify one or more documents URIs to read. This approach is for simple use
cases where you typically only need to retrieve a small number of documents. You can specify the URIs either via a
Mule DataWeave expression or by simply entering each one. 

If you use this approach, the following fields will be ignored if populated: Query, Query Type, Query Format, 
Collections, Directory, and Search Options.

### Using a String query

A [string query](https://docs.marklogic.com/guide/search-dev/string-query) uses MarkLogic's powerful search grammar
to express a query. To use this query, configure the following fields:

1. Put your query in the 'Query' field.
2. Set 'Query Type' to `STRING_QUERY`.
3. Ignore 'Query Format' (its value will have no effect).

### Using a Structured query

A [structured query](https://docs.marklogic.com/guide/search-dev/structured-query) is more complex than a string query
but allows you to represent any search expression supported by MarkLogic. To use this query, configure the following 
fields:

1. Put your structured query, expressed in either JSON or XML, in the 'Query' field.
2. Set 'Query Type' to `STRUCTURED_QUERY`.
3. Set 'Query Format' to either 'JSON' or 'XML'.

### Using a Serialized CTS query

A [serialized CTS query](https://docs.marklogic.com/guide/rest-dev/search#id_30577) is similar to a structured query 
but instead allows you to represent any type of MarkLogic query using 
[the MarkLogic CTS API](https://docs.marklogic.com/guide/search-dev/cts_query). To use this query, configure the 
following fields:

1. Put your serialized CTS query, expressed in either JSON or XML, in the 'Query' field.
2. Set 'Query Type' to 'SERIALIZED_CTS_QUERY'.
3. Set 'Query Format' to either 'JSON' or 'XML'.

### Using a Combined query

A [combined query](https://docs.marklogic.com/guide/rest-dev/search#id_69918) allows you to combine either a 
string query, structured query, or serialized CTS query with a set of 
[MarkLogic search options](https://docs.marklogic.com/guide/search-dev/query-options). To use this query, configure the
following fields:

1. Put your combined query, expressed in either JSON or XML, in the 'Query' field.
2. Set 'Query Type' to 'COMBINED_QUERY'.
3. Set 'Query Format' to either 'JSON' or 'XML'.

## Specifying metadata to include

For each matching document, the connector will construct a Mule message - specifically, 
[a Mule Result object](https://docs.mulesoft.com/mule-sdk/latest/result-object) - with a payload set to the contents
of the document. The URI and metadata associated with the document are then captured as a JSON object which comprises
the attributes of the Mule message. The "Document Metadata" field lets you control the metadata returned for each 
document; by default, no metadata is returned and only the URI will be present in the attributes. 

The allowable values for "Document Metadata" are: `all`, `collections`, `permissions`, `quality`, `metadatavalues`, 
and `properties`. 

The JSON object will then have the following keys based on the comma-delimited values you enter in "Document Metadata"
(and you can leave the field blank to only get back the URI for each document):

1. `uri` = the URI of the document.
2. `collections` = an array of collections that the document belongs to.
3. `permissions` = an object where each key is a role and the value is a set of capabilities.
4. `quality` = the quality score for the document.
5. `metadatavalues` = an object where each key and value correspond to a document metadata key and value.
6. `properties` = an object where each key is a string representation of an XML QName and the value is the XML element
associated with the QName in the document's properties fragment. 

## Controlling pagination

The "Read documents" operation returns a Mule streamable resource - specifically, an instance of the 
Mule `PagingProvider` class - that supports making multiple calls to MarkLogic to complete a query. Each call will 
retrieve up to the number of matching documents specified by the "Page Length" parameter, which defaults to 100.
This setting allows you to avoid timeouts when a query matches a large number of documents to also tune the performance
of the operation. Depending on the size of your documents, you may gain better performance via a smaller page length
for larger documents and a much higher page length for small documents, such as those with only a dozen or so fields. 

You may also use the "Max Results" field to put a limit on the number of documents returned. This is intended for use
cases where you may only need a representative set of data and not the full set of documents that match your query.

## Transforming results

A [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms) can be used to transform each 
document matching your query. You can specify the name of the transform via the "Transform" field. Any parameters 
needed by your transform can be specified as a comma-delimited string in the "Transform Parameters" with a pattern of
"parameter name,value,parameter name,value,etc". Finally, if you need to use a delimiter other than a comma - e.g. if
one of your parameter values has a comma in it - you can specify that delimiter with the "Transform Parameters 
Delimiter" field.


