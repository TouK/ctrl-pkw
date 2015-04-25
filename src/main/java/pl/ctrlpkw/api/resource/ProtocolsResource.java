package pl.ctrlpkw.api.resource;

import com.cloudinary.Cloudinary;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.constraint.VotesCountValid;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.PictureUploadToken;
import pl.ctrlpkw.api.filter.AuthorizationRequired;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.model.write.Ballot;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.ProtocolAccessor;
import pl.ctrlpkw.model.write.Ward;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Protokoly")
@Path("/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
@ClientVersionCheck
public class ProtocolsResource {

    @Resource
    private Mapper<Protocol> protocolMapper;

    @Resource
    private ProtocolAccessor protocolAccessor;

    @Resource
    private Cloudinary cloudinary;

    @Context
    private HttpServletRequest servletRequest;

    public String getCloudinaryCloudName() {
        return cloudinary.config.cloudName != null ? cloudinary.config.cloudName : "CLOUDINARY_CLOUD_NAME";
    }

    @ApiOperation("Przesłanie informacji o wynikach głosowania w obwodzie dla wszystkich kart")
    @ApiResponses({@ApiResponse(code = 200, message = "Protokół przyjęty do przetwarzania", response = PictureUploadToken.class)})
    @POST
    public Response create(@Valid @VotesCountValid pl.ctrlpkw.api.dto.Protocol protocol) {

        Optional<PictureUploadToken> pictureUploadToken = Optional.ofNullable(
                cloudinary.config.apiKey != null ? authorizePictureUpload() : null
        );

        UUID protocolId = saveProtocol(protocol, pictureUploadToken);

        pictureUploadToken.ifPresent(
                value -> value.setProtocolId(protocolId)
        );

        return Response.ok(pictureUploadToken.orElse(null)).build();
    }

    @ApiOperation("")
    @AuthorizationRequired
    @GET
    public Iterable<pl.ctrlpkw.api.dto.Protocol> readSome(@QueryParam("count") @DefaultValue("5") int count) {
        Result<Protocol> protocols = protocolAccessor.findNotVerified(count);
        return StreamSupport.stream(protocols.spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    @ApiOperation("Pobranie przesłanej informacji o wyniku głosowania w obwodzie")
    @GET
    @Path("{id}")
    public pl.ctrlpkw.api.dto.Protocol readOne(@ApiParam @PathParam("id") UUID id) {
        Protocol protocol = protocolAccessor.findById(id);
        return entityToDto.apply(protocol);
    }

    @ApiOperation(value = "Pobranie URLi wszystkich zdjęć dla protokołu", response = URI.class, responseContainer = "Set")
    @GET
    @Path("{id}/image")
    public Collection<URI> listImages(@ApiParam @PathParam("id") UUID id) {
        Protocol protocol = protocolAccessor.findById(id);
        return protocol.getImageIds().stream()
                .map(publicId -> URI.create("http://res.cloudinary.com/" + protocol.getCloudinaryCloudName() + "/image/upload/" + publicId))
                .collect(Collectors.toSet());
    }

    @ApiOperation("Przesłanie kolejnego zdjęcie protokołu")
    @ApiResponses({@ApiResponse(code = 200, message = "Można wysłać kolejne zdjęcie", response = PictureUploadToken.class)})
    @POST
    @Path("{id}/image")
    public Response authorizeNextImage(@ApiParam @PathParam("id") UUID id) {
        Protocol protocol = protocolAccessor.findById(id);
        if (protocol == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<PictureUploadToken> pictureUploadToken = Optional.ofNullable(
                cloudinary.config.apiKey != null ? authorizePictureUpload() : null
        );

        pictureUploadToken.ifPresent(
                value -> {
                    value.setProtocolId(id);
                    protocol.getImageIds().add(value.getPublicId());
                    protocolMapper.save(protocol);
                }
        );

        return Response.ok(pictureUploadToken.orElse(null)).build();

    }

    public enum VerificationResult { APPROVAL, DEPRECATION }

    @ApiOperation(value = "", authorizations = @Authorization("oauth2"))
    @POST
    @Path("{id}/verifications")
    @AuthorizationRequired
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verify(@Context HttpServletRequest servletRequest, @ApiParam @PathParam("id") UUID id, @ApiParam(required = true, allowableValues = "\"APPROVAL\", \"DEPRECATION\"") @NotNull VerificationResult result) {
        Account account = AccountResolver.INSTANCE.getAccount(servletRequest);
        Protocol protocol = protocolAccessor.findById(id);
        if (protocol == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        switch (VerificationResult.valueOf(String.valueOf(result))) {
            case APPROVAL:
                protocolAccessor.addApproval(protocol.getWard(), protocol.getBallot(), id, Sets.newHashSet(account.getUsername()));
                return Response.accepted().build();
            case DEPRECATION:
                protocolAccessor.addDeprecation(protocol.getWard(), protocol.getBallot(), id, Sets.newHashSet(account.getUsername()));
                return Response.accepted().build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private UUID saveProtocol(pl.ctrlpkw.api.dto.Protocol protocol, Optional<PictureUploadToken> pictureUploadToken) {
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
                .comment(protocol.getComment())
                .verified(false)
                .build();
        pictureUploadToken.ifPresent(
                value -> localBallotResult.setImageIds(Sets.newHashSet(value.getPublicId()))
        );
        if (AccountResolver.INSTANCE.hasAccount(servletRequest)) {
            Account account = AccountResolver.INSTANCE.getAccount(servletRequest);
            localBallotResult.setApprovals(Collections.singleton(account.getUsername()));
        }
        protocolMapper.save(localBallotResult);
        return localBallotResult.getId();
    }

    private PictureUploadToken authorizePictureUpload() {

        UUID publicId = UUID.randomUUID();
        int timestamp = (int) (System.currentTimeMillis() / 1000L);

        String signature = cloudinary.apiSignRequest(
                ImmutableMap.of(
                        "public_id", publicId,
                        "timestamp", timestamp),
                cloudinary.config.apiSecret
        );

        return PictureUploadToken.builder()
                .apiKey(cloudinary.config.apiKey)
                .publicId(publicId)
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
                    .comment(entity.getComment())
                    .build();
}
