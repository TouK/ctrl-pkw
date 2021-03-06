package pl.ctrlpkw.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import pl.ctrlpkw.Application;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.Protocol;
import pl.ctrlpkw.api.dto.Ward;
import pl.ctrlpkw.api.resource.ProtocolsResource;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class} )
@WebAppConfiguration
@Slf4j
public class ProtocolsGatheringIT extends IntegrationTestBase {

    @Value("${test.protocolsGathering.shortRun:true}")
    private boolean shortRun;

    @Test
    public void shouldAcceptProtocolsCountVotesAndSetProtocolStatusesInWards() throws Exception {
        givenEmptyResultsCache();
        givenNoProtocolsInDatabase();
        String firstClientId = UUID.randomUUID().toString();
        String secondClientId = null;

        whenFirstRound2010ProtocolsSent(firstClientId);
        if (shortRun) {
            whenFirstRound2010ProtocolsSent(firstClientId);//again
            whenVotesCountingRequested();
            thenAllWardsHaveProtocolStatus(Ward.ProtocolStatus.VAGUE);
        }

        whenFirstRound2010ProtocolsSent(secondClientId);
        whenVotesCountingRequested();
        thenAllWardsHaveProtocolStatus(Ward.ProtocolStatus.CONFIRMED);

        thenResultsAreSameAsIn2010();
    }

    protected void whenFirstRound2010ProtocolsSent(String clientId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ProtocolsResource.CLIENT_ID_HEADER, clientId);
        FlatFileItemReader<FieldSet> reader = createTestResultsReader();
        reader.open(new ExecutionContext());
        for (FieldSet item = reader.read(); item != null; item = reader.read()) {
            Protocol protocol = buildProtocolFromFieldSet(item);
            HttpEntity<Protocol> entity = new HttpEntity<>(protocol, headers);
            restTemplate.postForEntity(PROTOCOLS_URL, entity, Object.class, serverPort, "2010-06-20", 1);
        }
        reader.close();
    }

    protected Protocol buildProtocolFromFieldSet(FieldSet item) {
        return Protocol.builder()
                .votingDate(LocalDate.parse("2010-06-20"))
                .ballotNo(1)
                .communityCode(item.readString(2))
                .wardNo(item.readInt(6))
                .ballotResult(BallotResult.builder()
                        .votersEntitledCount(item.readLong(10))
                        .ballotsGivenCount(item.readLong(11))
                        .votesCastCount(item.readLong(12))
                        .votesValidCount(item.readLong(13))
                        .votesCountPerOption(
                                IntStream.rangeClosed(1, 10)
                                        .boxed()
                                        .map(pos -> item.readLong(13 + pos))
                                        .collect(Collectors.toList())
                        )
                        .build())
                .build();
    }

    protected void thenAllWardsHaveProtocolStatus(Ward.ProtocolStatus status) throws Exception {
        FlatFileItemReader<FieldSet> reader = createTestResultsReader();
        reader.open(new ExecutionContext());
        for (FieldSet item = reader.read(); item != null; item = reader.read()) {
            String communityCode = item.readString(2);
            int wardNo = item.readInt(6);
            try {
                thenWardHasProtocolStatus(communityCode, wardNo, status);
            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() != HttpStatus.NOT_FOUND) {
                    throw ex;
                } else {
                    log.warn("Ward {}/{} not found", communityCode, wardNo);
                }
            }
        }
        reader.close();
    }

    protected void thenResultsAreSameAsIn2010() {
        BallotResult ballotResult = restTemplate.getForObject(RESULT_URL, BallotResult.class, serverPort, "2010-06-20", 1);
        assertThat(ballotResult.getIncludedWardsCount()).isEqualTo(shortRun ? 100 : 25774);
        assertThat(ballotResult.getVotersEntitledCount()).isEqualTo(shortRun ? 138268 : 30813005);
        assertThat(ballotResult.getBallotsGivenCount()).isEqualTo(shortRun ? 69516 : 16929088);
        assertThat(ballotResult.getVotesCastCount()).isEqualTo(shortRun ? 69497 : 16923832);
        assertThat(ballotResult.getVotesValidCount()).isEqualTo(shortRun ? 68979 : 16806170);
        assertThat(ballotResult.getVotesCountPerOption()).isEqualTo(
                shortRun ?
                        Arrays.asList(556l, 20106l, 33289l, 1694l, 826l, 124l, 10585l, 1001l, 686l, 112l) :
                        Arrays.asList(177315l, 6128255l, 6981319l, 416898l, 214657l, 21596l, 2299870l, 242439l, 294273l, 29548l)
        );

    }

    private FlatFileItemReader<FieldSet> createTestResultsReader() {
        FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("pzt2010-wyn-obw.csv"));
        reader.setEncoding("Cp1250");
        reader.setLinesToSkip(1);
        if (shortRun) {
            reader.setMaxItemCount(100);
        }
        reader.setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";"));
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
        return reader;
    }

}
