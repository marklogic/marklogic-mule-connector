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
