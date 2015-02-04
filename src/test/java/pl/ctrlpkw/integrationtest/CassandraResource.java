package pl.ctrlpkw.integrationtest;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.rules.ExternalResource;

class CassandraResource extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

}