package pl.ctrlpkw.api.resource;

import com.cloudinary.Cloudinary;
import com.datastax.driver.mapping.Mapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.CassandraContext;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.PictureUploadToken;
import pl.ctrlpkw.model.write.Ballot;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.Ward;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Api("Protokoły")
@Path("/votings/{date}/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
public class ProtocolsResource {

    @Resource
    private CassandraContext cassandraContext;

    @Resource
    private Cloudinary cloudinary;
    
    @ApiOperation("Przesłanie informacji o wynikach głosowania w obwodzie dla wszystkich kart")
    @ApiResponses({@ApiResponse(code = 202, message = "Protokół przyjęty do przetważania", response = PictureUploadToken.class)})
    @POST
    public Response create(@ApiParam @PathParam("date") String votingDate, @Valid pl.ctrlpkw.api.dto.Protocol protocol) {
        List<PictureUploadToken> result = new LinkedList<>();
        for (BallotResult ballotResult : Optional.fromNullable(protocol.getBallotResults()).or(Lists.<BallotResult>newArrayList())) {
            UUID uuid = saveBallotLocalResult(LocalDate.parse(votingDate).toDate(), protocol.getCommunityCode(), protocol.getWardNo(), ballotResult);
            if (cloudinary.config.apiKey != null) {
                result.add(
                        authorizePictureUpload(uuid)
                );
            }
        }
        return Response.accepted(result).build();
    }

    private UUID saveBallotLocalResult(Date votingDate, String communityCode, Integer wardNo, BallotResult ballotResult) {
        Protocol localBallotResult = Protocol.builder()
                .id(UUID.randomUUID())
                .ballot(Ballot.builder().votingDate(votingDate).no(ballotResult.getBallotNo()).build())
                .ward(Ward.builder().communityCode(communityCode).no(wardNo).build())
                .votersEntitledCount(ballotResult.getVotersEntitledCount())
                .ballotsGivenCount(ballotResult.getBallotsGivenCount())
                .votesCastCount(ballotResult.getVotesCastCount())
                .votesValidCount(ballotResult.getVotesValidCount())
                .votesCountPerOption(
                        ballotResult.getVotesCountPerOption()
                )
                .cloudinaryCloudName(cloudinary.config.cloudName)
                .build();
        Mapper<Protocol> mapper = cassandraContext.getMappingManager().mapper(Protocol.class);
        mapper.save(localBallotResult);
        return localBallotResult.getId();
    }
    
    private PictureUploadToken authorizePictureUpload(UUID publicId) {
        
        int timestamp = (int) (System.currentTimeMillis() / 1000L);

        String signature = cloudinary.apiSignRequest(
                ImmutableMap.of(
                        "public_id", publicId,
                        "timestamp", timestamp),
                cloudinary.config.apiSecret
        );

        return PictureUploadToken.builder()
                .apiKey(cloudinary.config.apiKey)
                .publicId(String.valueOf(publicId))
                .timestamp(timestamp)
                .signature(signature)
                .build();
    }
}
