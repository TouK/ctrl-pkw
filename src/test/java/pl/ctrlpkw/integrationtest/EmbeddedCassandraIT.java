package pl.ctrlpkw.integrationtest;

import org.junit.ClassRule;
import pl.ctrlpkw.CassandraContext;

import javax.annotation.Resource;

public abstract class EmbeddedCassandraIT {

    public static final String CASSANDRA_CONFIG = "cassandra.port:9142";

    @ClassRule
    public static CassandraResource cassandra = new CassandraResource();

    @Resource
    protected CassandraContext cassandraContext;

}
