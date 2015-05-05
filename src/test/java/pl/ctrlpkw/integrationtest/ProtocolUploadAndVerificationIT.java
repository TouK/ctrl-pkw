package pl.ctrlpkw.integrationtest;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import pl.ctrlpkw.Application;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.PictureUploadToken;
import pl.ctrlpkw.api.dto.Protocol;
import pl.ctrlpkw.api.dto.Ward;
import pl.ctrlpkw.api.resource.ProtocolsResource;
import pl.ctrlpkw.model.read.WardRepository;

import javax.annotation.Resource;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ProtocolUploadAndVerificationIT extends IntegrationTestBase {

    public static final String WARDS_URL = "http://localhost:{serverPort}/api/votings/{votingDate}/wards?latitude={latitude}&longitude={longitude}";

    @Value("${local.server.port}")
    private String serverPort;

    @Resource
    private WardRepository wardRepository;

    @Test
    public void shouldPassFlowWithProtocolVerification() throws InterruptedException {

        givenEmptyResultsCache();
        givenNoProtocolsInDatabase();

        //givenSomeLocation
        double latitude = 52.0693;
        double longitude = 19.480193;

        //givenSomeProtocolData
        BallotResult localBallotResult = BallotResult.builder()
                .votersEntitledCount(2048l)
                .ballotsGivenCount(1536l)
                .votesCastCount(1280l)
                .votesValidCount(1023l)
                .votesCountPerOption(
                        Lists.newArrayList(1l, 2l, 4l, 8l, 16l, 32l, 64l, 128l, 256l, 512l)
                )
                .build();

        //whenClosestWardSelected
        Ward closestWard = restTemplate.getForObject(
                WARDS_URL, Ward[].class, serverPort, "2010-06-20",
                Double.toString(latitude), Double.toString(longitude))[0];

        //whenProtocolDataSent
        Protocol protocol = Protocol.builder()
                .votingDate(LocalDate.parse("2010-06-20"))
                .ballotNo(1)
                .communityCode(closestWard.getCommunityCode())
                .wardNo(closestWard.getNo())
                .comment("test")
                .ballotResult(localBallotResult)
                .build();
        protocol = restTemplate.postForObject(PROTOCOLS_URL, protocol, Protocol.class, serverPort);

        whenPictureUploadAuthorizationRequested(protocol.getId());
        whenPictureUploadAuthorizationRequested(protocol.getId());
        whenVotesCountingRequested();
        thenWardHasProtocolStatus(closestWard.getCommunityCode(), closestWard.getNo(), Ward.ProtocolStatus.VAGUE);

        whenProtocolVerified(protocol.getId());
        whenVotesCountingRequested();
        thenWardHasProtocolStatus(closestWard.getCommunityCode(), closestWard.getNo(), Ward.ProtocolStatus.CONFIRMED);

    }

    protected void whenPictureUploadAuthorizationRequested(UUID protocolId) {
        PictureUploadToken pictureUploadToken = restTemplate.postForObject(IMAGES_URL, "", PictureUploadToken.class, serverPort, protocolId);
    }

    protected void whenProtocolVerified(UUID protocolId) {
        restTemplate.postForObject(VERIFICATIONS_URL, ProtocolsResource.VerificationResult.APPROVAL, Protocol.class, serverPort, protocolId);
    }

}
