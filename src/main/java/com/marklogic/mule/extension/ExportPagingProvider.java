package com.marklogic.mule.extension;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.InputStreamHandle;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ExportPagingProvider extends AbstractPagingProvider implements PagingProvider<DatabaseClient, InputStream> {

    ExportPagingProvider(QueryParameters params) {
        super(params);
    }

    @Override
    public List<InputStream> getPage(DatabaseClient databaseClient) {
        List<InputStream> results = new ArrayList<>();
        super.handlePage(databaseClient, documentRecord ->
            results.add(documentRecord.getContent(new InputStreamHandle()).get())
        );
        return results;
    }

    @Override
    public Optional<Integer> getTotalResults(DatabaseClient databaseClient) {
        return Optional.empty();
    }

    @Override
    public void close(DatabaseClient databaseClient) {
        // We don't want to call release here, as the client will no longer be usable by other operations.
    }
}
