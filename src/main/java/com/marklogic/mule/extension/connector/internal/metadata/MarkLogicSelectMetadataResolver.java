package com.marklogic.mule.extension.connector.internal.metadata;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

/**
 *
 *
 * @since 1.0.1
 *
 */
public class MarkLogicSelectMetadataResolver implements OutputTypeResolver<String> {
    @Override
    public String getResolverName() {
        return "MarkLogicSelectResolver";
    }

    @Override
    public MetadataType getOutputType(MetadataContext metadataContext, String s) throws MetadataResolvingException, ConnectionException {
        return null;
    }

    @Override
    public String getCategoryName() {
        return "MarkLogicCategory";
    }
}
