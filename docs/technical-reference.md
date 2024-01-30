# MarkLogic Connector Reference

## Configurations
```
<marklogic:config>
```
This configuration describes how the connector connects with the MarkLogic database

### Parameters
 | Name               | Type                                                                             | Description                                                  | Default Value | Required |
 |--------------------|----------------------------------------------------------------------------------|--------------------------------------------------------------| ------ |--|
 | host               | String                                                                           | The hostname of the MarkLogic server to connect to.          | localhost | X |
 | port               | Integer                                                                          | The port of the MarkLogic REST API app server to connect to. | 8000 | X |
 | authenticationType | Enumeration, one of: DIGEST, BASIC, MARKLOGIC_CLOUD, CERTIFICATE, KERBEROS, SAML | The authentication required by the MarkLogic REST API app server. | DIGEST | X |
 | connectionType     | Enumeration, one of: DIRECT, GATEWAY                                             | Set to GATEWAY when connecting to MarkLogic through a load balancer; otherwise select DIRECT. | DIRECT |  |
 | username           | String                                                                           | MarkLogic username to use when 'Authentication Type' is set to BASIC or DIGEST.          |  |  |
 | password           | String                                                                           | Password for the MarkLogic user when 'Authentication Type' is set to BASIC or DIGEST. |  |  |
 | cloudApiKey        | String                                                                           | API key to use when 'Authentication Type' is set to MARKLOGIC_CLOUD.          |  |  |
 | samlToken          | String                                                                           | SAML access token to use when 'Authentication Type' is set to SAML.          |  |  |
 | kerberosPrincipal  | String                                                                           | Kerberos principal to use when 'Authentication Type' is set to KERBEROS.          |  |  |
 | basePath           | String                                                                           | Base path for each request to MarkLogic; typically used when connecting through a reverse proxy; required by MarkLogic Cloud.          |  |  |
 | database           | String                                                                           | Identifies the MarkLogic content database to query; only required when the database associated with the app server identified by the 'Port' value is not the one you wish to query.          |  |  |
 | tlsContextFactory  | TlsContextFactory                                                                | Controls how SSL/TLS connections are made with MarkLogic.          |  |  |
 | hostnameVerifier   | HostnameVerifier                                                                 | Specifies how a hostname is verified during SSL authentication. COMMON allows any level of subdomain for SSL certificates with wildcard domains. STRICT only allows one subdomain level for SSL certificates with wildcard domains. ANY disables hostname verification and is not recommended for production usage.          |  |  |

## Operations

### Read Documents
```
<marklogic:read-documents>
```
Returns the content, URI, and optional metadata for each document matching the query criteria.

### Parameters
| Name       | Type             | Description | Default | Value | Required |
|------------|------------------|------|------|------|------|
| config-ref | marklogic:config | The name of the configuration to be used to execute this component. | | | X |
| uris       | List\<String\>   | Specify one or more document URIs to read. If specified, all other parameters for querying will be ignored. | | | |
| query  | String             | A MarkLogic query that corresponds to the type defined by 'Query Type', which must be set if a query is provided here. | | | |
| queryType | Enumeration, one of: STRING_QUERY, STRUCTURED_QUERY, SERIALIZED_CTS_QUERY, COMBINED_QUERY | A MarkLogic query that corresponds to the type defined by 'Query Type', which must be set if a query is provided here. | | | |
| queryFormat | Enumeration, one of: JSON, XML | The type of MarkLogic query to execute. Please see the connector user guide for more information on each type of query. | | | |
| collections | String | Comma-delimited collections to filter results. | | | |
| directory | String | Filters results to only include documents in the specified database directory. | | | |
| searchOptions | String | The name of the REST search options to apply to the query defined in 'Query'. Please see the connector user guide for more information. | | | |
| categories | String | Comma-delimited list of the types of metadata to include for each matching document. Allowable values are: all, collections, permissions, properties, quality, and metadatavalues. | | | |
| maxResults | Integer | Maximum number of documents to retrieve. | | | |
| pageLength | Integer | Number of documents to retrieve in each page, which corresponds to a call to MarkLogic. | | | |
| transform | String | Name of a REST transform to apply to each matching document. | | | |
| transformParameters | String | Comma-delimited parameters to pass to the REST transform. | | | |
| transformParametersDelimiter | String | Delimiter to use for defining 'Transform Parameters'. | | | |
| consisistenSnapshot | boolean | Whether to constrain each page of results to the same MarkLogic server timestamp. | | | |

### Write Documents
```
<marklogic:write-documents>
```
Write one or more documents to MarkLogic, typically from within a Mule 'Batch Aggregator'.

### Parameters
| Name           | Type                                                  | Description | Default | Value | Required |
|----------------|-------------------------------------------------------|------|------|------|---|
| config-ref     | marklogic:config                                      | The name of the configuration to be used to execute this component. |      | | X |
| contents       | InputStream[]                                         | An array containing InputStream objects with the contents of each document | | | X |
| documentFormat | Enumeration, one of: BINARY, JSON, TEXT, XML, UNKNOWN | Format of documents to write to MarkLogic. Selecting 'UNKNOWN' will result in MarkLogic determining the format based on the URI extension. | JSON | |   |
| collections | String | Comma-delimited collections to assign to each document. | | | |
| permissions | String | Comma-delimited permissions to assign to each document; format is role,capability,role,capability. | | | |
| temporalCollections | String | Temporal collection to assign each document to. | | | |
| quality | int | Quality score to assign to each document. | | | |
| uriPrefix | String | String to prepend to each document URI. | | | |
| uriSuffix | String | String to append to each document URI. | | | |
| generateUUID | boolean | Whether to include a UUID in each document URI. | | | |
| transform | String | Name of a REST transform to apply to each document. | | | |
| transformParameters | String | Comma-delimited parameters to pass to the REST transform. | | | |
| transformParametersDelimiter | String | Delimiter to use for defining 'Transform Parameters'. | | | |

