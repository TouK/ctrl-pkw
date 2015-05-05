package pl.ctrlpkw.integrationtest;

import org.junit.ClassRule;
import org.springframework.boot.test.IntegrationTest;
import pl.ctrlpkw.CassandraContext;

import javax.annotation.Resource;

@IntegrationTest({
        "server.port:0",
        "cassandra.port:9142",
        "stormpath.enabled:false"
})
public abstract class IntegrationTestBase {

    @ClassRule
    public static CassandraResource cassandra = new CassandraResource();

    @Resource
    protected CassandraContext cassandraContext;

}
