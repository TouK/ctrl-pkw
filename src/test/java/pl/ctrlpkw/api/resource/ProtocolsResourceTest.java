package pl.ctrlpkw.api.resource;

import com.cloudinary.Cloudinary;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.PictureUploadToken;
import pl.ctrlpkw.api.dto.Protocol;
import pl.ctrlpkw.model.write.Ballot;
import pl.ctrlpkw.model.write.ProtocolAccessor;
import pl.ctrlpkw.model.write.Ward;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProtocolsResourceTest {

    @Spy
    private Cloudinary cloudinary = new Cloudinary("cloudinary://key:secret@cloud");

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private Mapper<pl.ctrlpkw.model.write.Protocol> protocolMapper;

    @Mock
    private ProtocolAccessor protocolAccessor;

    @InjectMocks
    private ProtocolsResource protocolsResource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveProtocolAndRespondWithCloudinaryAuthorization() throws Exception {

        //given
        Protocol protocol = Protocol.builder()
                .votingDate(LocalDate.parse("2010-06-20"))
                .ballotNo(1)
                .communityCode("TEST")
                .wardNo(1)
                .comment("test")
                .ballotResult(BallotResult.builder()
                                .votersEntitledCount(2048l)
                                .ballotsGivenCount(1536l)
                                .votesCastCount(1280l)
                                .votesValidCount(1023l)
                                .votesCountPerOption(
                                        Lists.newArrayList(1l, 2l, 4l, 8l, 16l, 32l, 64l, 128l, 256l, 512l)
                                )
                                .build()
                )
                .build();

        //when
        Response response = protocolsResource.create(protocol);
        PictureUploadToken pictureUploadToken = (PictureUploadToken) response.getEntity();

        //then
        verify(protocolMapper).save(
                pl.ctrlpkw.model.write.Protocol.builder()
                        .id(any(UUID.class))
                        .ballot(Ballot.builder()
                                        .votingDate(protocol.getVotingDate().toDate())
                                        .no(protocol.getBallotNo())
                                        .build()
                        )
                        .ward(Ward.builder()
                                        .communityCode(protocol.getCommunityCode())
                                        .no(protocol.getWardNo())
                                        .build()
                        )
                        .comment(protocol.getComment())
                        .votersEntitledCount(protocol.getBallotResult().getVotersEntitledCount())
                        .ballotsGivenCount(protocol.getBallotResult().getBallotsGivenCount())
                        .votesCastCount(protocol.getBallotResult().getVotesCastCount())
                        .votesValidCount(protocol.getBallotResult().getVotesValidCount())
                        .votesCountPerOption(protocol.getBallotResult().getVotesCountPerOption())
                        .build()
        );
        assertThat(pictureUploadToken.getApiKey()).isEqualTo("key");
        assertThat(pictureUploadToken.getTimestamp()).isGreaterThan(0);
        assertThat(pictureUploadToken.getPublicId()).isNotEmpty();
        assertThat(pictureUploadToken.getSignature()).isNotEmpty();

    }

    @Configuration
    public static class Config {
    }
}