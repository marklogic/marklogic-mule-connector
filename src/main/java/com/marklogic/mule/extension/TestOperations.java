package com.marklogic.mule.extension;

import com.marklogic.mule.extension.api.DocumentAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;


public class TestOperations extends Operations{

    /**
     * Temp function
     */

    public InputStream[] readArray(Result<InputStream, DocumentAttributes> value)  {
        InputStream[] inputStreams = new InputStream[1];
        inputStreams[0] = value.getOutput();
System.out.println("*************");
        return inputStreams;
    }
}
