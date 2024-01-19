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
package com.marklogic.mule.connector.api.types;

import com.marklogic.client.io.Format;

/**
 * Class exists solely to avoid warnings from Mule about the Java Client's Format class being used in an operation
 * but not being in a package with "api" or "internal" in its name.
 */
public enum DocumentFormat {

    BINARY {
        @Override
        public Format getFormat() {
            return Format.BINARY;
        }
    },
    JSON {
        @Override
        public Format getFormat() {
            return Format.JSON;
        }
    },
    TEXT {
        @Override
        public Format getFormat() {
            return Format.TEXT;
        }
    },
    XML {
        @Override
        public Format getFormat() {
            return Format.XML;
        }
    },
    UNKNOWN {
        @Override
        public Format getFormat() {
            return Format.UNKNOWN;
        }
    };

    public abstract Format getFormat();
}
