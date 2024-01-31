/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2024 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

/*
 * MarkLogic Mule Connector
 * Copyright (c) 2024 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.*;

/**
 * Simple class for providing the input that write-documents needs.
 * Discovered how to create this based on the docs at:
 * https://docs.mulesoft.com/dataweave/latest/dataweave-cookbook-java-methods
 */
public class Data {

    public static InputStream[] getJsonDoc() {
        return new InputStream[]{new ByteArrayInputStream("{\"hello\":\"world\"}".getBytes())};
    }

    public static InputStream[] getTextDoc() {
        return new InputStream[]{new ByteArrayInputStream("Hello, World!".getBytes())};
    }

    public static InputStream[] getXmlDoc() {
        return new InputStream[]{new ByteArrayInputStream("<Hello>World</Hello>".getBytes())};
    }

    public static InputStream[] getBinaryDoc() throws IOException {
        return new InputStream[]{new FileInputStream(new File("test-app/src/main/ml-data/metadataSamples/binary/logo.png"))};
    }

    public static InputStream[] getTextAndJsonDocs() {
        return new InputStream[]{getTextDoc()[0], getJsonDoc()[0]};
    }

    public static InputStream[] getTemporalDoc() {
        String doc = "<tempdoc>\n" +
            "  <content>test document</content>\n" +
            "  <systemStart>2023-11-12T11:00:00</systemStart>\n" +
            "  <systemEnd>2023-11-14T16:00:00</systemEnd>\n" +
            "  <validStart>2023-11-12T11:00:00</validStart>\n" +
            "  <validEnd>2023-11-14T16:00:00</validEnd>\n" +
            "</tempdoc>";
        return new InputStream[]{new ByteArrayInputStream(doc.getBytes())};
    }
}
