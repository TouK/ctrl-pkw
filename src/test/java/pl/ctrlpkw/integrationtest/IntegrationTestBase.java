package pl.ctrlpkw.integrationtest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.cache.CacheManager;
import org.springframework.shell.support.util.FileUtils;
import org.springframework.web.client.RestTemplate;
import pl.ctrlpkw.Application;
import pl.ctrlpkw.CassandraContext;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.Ward;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

@IntegrationTest({
        "server.port:0",
        "cassandra.port:9142",
        "stormpath.enabled:false"
})
public abstract class IntegrationTestBase {

    public static final String RESULT_URL = "http://localhost:{port}/api/votings/{votingDate}/results/ballots/{ballotNo}";
    public static final String PROTOCOLS_URL = "http://localhost:{port}/api/protocols?authorizePictureUpload=false";
    public static final String VERIFICATIONS_URL = "http://localhost:{port}/api/protocols/{id}/verifications";
    public static final String IMAGES_URL = "http://localhost:{port}/api/protocols/{id}/image";
    public static final String WARD_URL = "http://localhost:{serverPort}/api/votings/{votingDate}/wards/{communityCode}/{wardNo}";

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

    @Resource
    private CacheManager redisCacheManager;

    @Value("${local.server.port}")
    protected String serverPort;

    protected void givenNoProtocolsInDatabase() {
        File schemaScript = FileUtils.getFile(Application.class, "/schema.cql");
        Arrays.stream(contentOf(schemaScript).split(";"))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotEmpty)
                .forEach(line -> cassandraContext.getSession().execute(line));
    }

    protected void givenEmptyResultsCache() {
        redisCacheManager.getCacheNames().stream()
                .map(redisCacheManager::getCache)
                .forEach(cache -> cache.clear());
    }

    protected void whenVotesCountingRequested() {
        restTemplate.postForObject(RESULT_URL, null, BallotResult.class, serverPort, "2010-06-20", 1);
    }

    protected void thenWardHasProtocolStatus(String communityCode, int wardNo, Ward.ProtocolStatus status) {
        Ward ward = restTemplate.getForObject(WARD_URL, Ward.class, serverPort, "2010-06-20", communityCode, wardNo);
        assertThat(ward.getProtocolStatus()).isEqualTo(status);
    }


}
