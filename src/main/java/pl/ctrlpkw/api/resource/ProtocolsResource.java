package pl.ctrlpkw.api.resource;

import com.cloudinary.Cloudinary;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.ImmutableMap;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Api("Protokoły")
@Path("/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
public class ProtocolsResource {

    @Resource
    private CassandraContext cassandraContext;

    @Resource
    private Cloudinary cloudinary;

    public String getCloudinaryCloudName() {
        return cloudinary.config.cloudName != null ? cloudinary.config.cloudName : "CLOUDINARY_CLOUD_NAME";
    }

    @ApiOperation("Przesłanie informacji o wynikach głosowania w obwodzie dla wszystkich kart")
    @ApiResponses({@ApiResponse(code = 202, message = "Protokół przyjęty do przetważania", response = PictureUploadToken.class)})
    @POST
    public Response create(@Valid pl.ctrlpkw.api.dto.Protocol protocol) {
        List<PictureUploadToken> result = new LinkedList<>();
            UUID uuid = saveProtocol(protocol);
            if (cloudinary.config.apiKey != null) {
                result.add(
                        authorizePictureUpload(uuid)
                );
            }
        return Response.accepted(result).build();
    }

    @ApiOperation("Pobranie przesłanej informacji o wyniku głosowania w obwodzie")
    @GET
    @Path("{id}")
    public pl.ctrlpkw.api.dto.Protocol readOne(@ApiParam @PathParam("id") UUID id) {
        Mapper<Protocol> mapper = cassandraContext.getMappingManager().mapper(Protocol.class);
        Protocol protocol = mapper.get(id);
        return entityToDto.apply(protocol);
    }

    private UUID saveProtocol(pl.ctrlpkw.api.dto.Protocol protocol) {
        Protocol localBallotResult = Protocol.builder()
                .id(UUID.randomUUID())
                .ballot(Ballot.builder().votingDate(protocol.getVotingDate().toDate()).no(protocol.getBallotNo()).build())
                .ward(Ward.builder().communityCode(protocol.getCommunityCode()).no(protocol.getWardNo()).build())
                        .votersEntitledCount(protocol.getBallotResult().getVotersEntitledCount())
                        .ballotsGivenCount(protocol.getBallotResult().getBallotsGivenCount())
                        .votesCastCount(protocol.getBallotResult().getVotesCastCount())
                        .votesValidCount(protocol.getBallotResult().getVotesValidCount())
                .votesCountPerOption(
                        protocol.getBallotResult().getVotesCountPerOption()
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

    private static Function<Protocol, pl.ctrlpkw.api.dto.Protocol> entityToDto = entity ->
            pl.ctrlpkw.api.dto.Protocol.builder()
                    .id(entity.getId())
                    .votingDate(LocalDate.fromDateFields(entity.getBallot().getVotingDate()))
                    .ballotNo(entity.getBallot().getNo())
                    .communityCode(entity.getWard().getCommunityCode())
                    .wardNo(entity.getWard().getNo())
                    .ballotResult(
                            BallotResult.builder()
                                    .votersEntitledCount(entity.getVotersEntitledCount())
                                    .ballotsGivenCount(entity.getBallotsGivenCount())
                                    .votesCastCount(entity.getVotesCastCount())
                                    .votesValidCount(entity.getVotesValidCount())
                                    .votesCountPerOption(entity.getVotesCountPerOption())
                                    .build()
                    )
                    .build();
}
