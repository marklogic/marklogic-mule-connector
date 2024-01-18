package com.marklogic.mule.connector.api.provider;

import com.marklogic.client.DatabaseClientFactory;

/**
 * Mirrors the Java Client's {@code SSLHostnameVerifier} class; this exists solely to avoid breaking changes if the
 * Java Client class were to change.
 */
public enum HostnameVerifier {

    ANY {
        @Override
        public DatabaseClientFactory.SSLHostnameVerifier getSslHostnameVerifier() {
            return DatabaseClientFactory.SSLHostnameVerifier.ANY;
        }
    },

    COMMON {
        @Override
        public DatabaseClientFactory.SSLHostnameVerifier getSslHostnameVerifier() {
            return DatabaseClientFactory.SSLHostnameVerifier.COMMON;
        }
    },

    STRICT {
        @Override
        public DatabaseClientFactory.SSLHostnameVerifier getSslHostnameVerifier() {
            return DatabaseClientFactory.SSLHostnameVerifier.STRICT;
        }
    };

    public abstract DatabaseClientFactory.SSLHostnameVerifier getSslHostnameVerifier();
}
