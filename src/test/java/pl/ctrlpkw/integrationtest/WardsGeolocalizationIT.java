package pl.ctrlpkw.integrationtest;

import com.google.common.collect.Iterators;
import pl.ctrlpkw.Application;
import pl.ctrlpkw.api.dto.Ward;
import pl.ctrlpkw.model.read.WardRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port:0", EmbeddedCassandraIT.CASSANDRA_CONFIG})
public class WardsGeolocalizationIT extends EmbeddedCassandraIT {

    public static final String WARDS_URL = "http://localhost:{serverPort}/api/votings/{votingDatew}/wards?latitude={latitude}&longitude={longitude}";

    @Value("${local.server.port}")
    private String serverPort;

    @Resource
    private WardRepository wardRepository;

    private RestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void shouldReturnWardWhenGivenExactLocation() throws InterruptedException {
        //given
        pl.ctrlpkw.model.read.Ward ward = Iterators.getNext(wardRepository.findAll(new PageRequest(30, 1)).iterator(), null);

        //when
        ResponseEntity<Ward[]> closestWards = restTemplate.getForEntity(
                WARDS_URL, Ward[].class, serverPort, "2010-06-20",
                Double.toString(ward.getLocation().getX()), Double.toString(ward.getLocation().getY()));

        //then
        assertThat(
                Iterators.<Ward>any(Arrays.asList(closestWards.getBody()).iterator(), input ->
                                ward.getCommunityCode().equals(input.getCommunityCode())
                                        && ward.getWardNo().equals(input.getNo())
                )).isTrue();
    }

}
