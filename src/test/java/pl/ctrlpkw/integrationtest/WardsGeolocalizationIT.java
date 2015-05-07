package pl.ctrlpkw.integrationtest;

import com.google.common.collect.Iterators;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import pl.ctrlpkw.Application;
import pl.ctrlpkw.api.dto.Ward;
import pl.ctrlpkw.model.read.WardRepository;

import javax.annotation.Resource;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class WardsGeolocalizationIT extends IntegrationTestBase {

    public static final String WARDS_URL = "http://localhost:{serverPort}/api/votings/{votingDatew}/wards?latitude={latitude}&longitude={longitude}&radius={radius}&minCount={minCount}";

    @Resource
    private WardRepository wardRepository;

    @Test
    public void shouldReturnWardWhenGivenExactLocation() throws InterruptedException {
        //given
        pl.ctrlpkw.model.read.Ward ward = Iterators.getNext(wardRepository.findAll(new PageRequest(30, 1)).iterator(), null);

        //when
        ResponseEntity<Ward[]> closestWards = restTemplate.getForEntity(
                WARDS_URL, Ward[].class, serverPort, "2010-06-20",
                Double.toString(ward.getLocation().getY()), Double.toString(ward.getLocation().getX()), 1, 1);

        //then
        assertThat(
                Iterators.any(Arrays.asList(closestWards.getBody()).iterator(), input ->
                                ward.getCommunityCode().equals(input.getCommunityCode())
                                        && ward.getWardNo().equals(input.getNo())
                )).isTrue();
    }

    @Test
    public void shouldReturnAllWardsInTheSameLocationEvenWhenAskedForOneClosest() throws InterruptedException {
        //given

        //when
        ResponseEntity<Ward[]> closestWards = restTemplate.getForEntity(
                WARDS_URL, Ward[].class, serverPort,
                "2010-06-20", Double.toString(52.2159212), Double.toString(20.9678000), 1, 1
        );

        //then
        assertThat(closestWards.getBody().length).isGreaterThan(2);
        assertThat(closestWards.getBody()[0].getLocation()).isEqualTo(closestWards.getBody()[0].getLocation());
    }

}
