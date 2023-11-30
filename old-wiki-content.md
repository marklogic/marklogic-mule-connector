Capturing content here that is being removed from the GitHub Wiki since it only applies
to the 1.x connector.

# Best Practices

As of release 1.1.1, we're documenting best practices to help ease the learning curve using MuleSoft and the Connector.

## Connector Versioning

* Use the latest version of the Connector, acquired from Anypoint Exchange, instead of compiling from this source repository.  The latter should only be needed if you're custom developing operations.

## Java Versioning

* Use Anypoint Studio's built-in Java libraries, as opposed to any other JDKs on your local environment.
* Be aware that Java 8 usage is a requirement at minimum in Mule development.  Java 11 can also be used against Mule runtime engine 4.2.0 or higher.
* <a href="https://help.mulesoft.com/s/article/Mule-Runtime-support-for-Java-11-FAQ">Learn more about Java compatibility</a>.

## Importing Content into MarkLogic with `importDocs`

* Our experience has shown the best way to work with Mule streaming content into MarkLogic via the `importDocs` operation, is to immediately precede that operation with either the built-in Set Payload operation, or the Transform Message operation.
* Both operations provide the capability of expressing the input MIME type and character encoding of the data going into the importDocs operation, which helps the underlying DMSDK determine how best to write the content to MarkLogic. Wherever possible, set MIME types and character encodings (binary documents won't need an encoding).
    * As a reminder, MarkLogic expects UTF-8 character encoding for JSON, XML, and text.
* For XML only:
    * Use the Transform Message operation and the DataWeave 2.0 language to prepare the incoming payload content from an XML string to an XML element: `%dw 2.0 output application/xml -- read(payload, 'application/xml')`. This would set the `payload` input to `importDocs` as a proper XML node.
* For JSON, Text, or binary documents:
    * Use Set Payload to prepare the payload with a MIME type and a character encoding.

## Use Mule Batch Processing

* We recommend using <a href="https://docs.mulesoft.com/mule-runtime/4.3/batch-processing-concept">MuleSoft's built-in batch processing</a>, which not only achieves roughly the same threading, batching, etc. available in DMSDK, but also processes job reports in a similar way.

Most of the the MarkLogic Connector operations are built atop DMSDK, so there's no escaping usage of DMSDK batching.  However, it works well in tandem with Mule batch processing, provided you keep the batch size and thread counts the same.  You can manage this by setting those as constants in `automation-credentials.properties` so that both the MarkLogic Connection configuration and the Mule Batch configuration can leverage them.

See the <a href="/marklogic-community/marklogic-mule-connector/tree/master/examples/importDocs/batch">importDocs batch flow</a> for example usage.