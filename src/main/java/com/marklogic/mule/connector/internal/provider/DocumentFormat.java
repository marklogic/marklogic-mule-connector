package com.marklogic.mule.connector.internal.provider;

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
