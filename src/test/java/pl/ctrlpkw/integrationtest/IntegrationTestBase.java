package pl.ctrlpkw.integrationtest;

import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.web.client.RestTemplate;
import pl.ctrlpkw.CassandraContext;

import javax.annotation.Resource;

@IntegrationTest({
        "server.port:0",
        "cassandra.port:9142",
        "stormpath.enabled:false"
})
public abstract class IntegrationTestBase {

    protected RestTemplate restTemplate = new RestTemplate();

    @Resource
    private ClientVersionHeaderInterceptor clientVersionHeaderInterceptor;

    @Before
    public void addRestTemplateInterceptor() {
        restTemplate.getInterceptors().add(clientVersionHeaderInterceptor);
    }

    @ClassRule
    public static CassandraResource cassandra = new CassandraResource();

    @Resource
    protected CassandraContext cassandraContext;

}
