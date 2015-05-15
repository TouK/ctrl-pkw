package pl.ctrlpkw.batch;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Configuration
public class WardsLoadingBatchConfig {

    @Resource
    private GeometryFactory geometryFactory;

    @Resource
    private VotingRepository votingRepository;

    @Bean
    public ItemReader<FieldSet> wards2010Reader() {
        FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("pzt2010-obwody.csv"));
        reader.setEncoding("Cp1250");
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";"));
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<FieldSet, Ward> wards2010Processor() {
        ArrayList<Voting> votings = Lists.newArrayList(
                votingRepository.findByDate(Arrays.asList(LocalDate.parse("2010-06-20")))
        );
        return item -> {
            Ward ward = new Ward();
            ward.setVotings(votings);
            ward.setCommunityCode(item.readString(2));
            ward.setWardNo(item.readInt(6));
            ward.setWardAddress(item.readString(7));
            ward.setLabel("Obwodowa komisja wyborcza: " + item.readString(3).split(",", 2)[0] + " nr " + item.readString(6));
            ward.setShortLabel("Komisja nr " + item.readString(6));
            if (item.getFieldCount() >= 15) {
                ward.setLocation(geometryFactory.createPoint(new Coordinate(
                        item.readDouble(14),
                        item.readDouble(13)
                )));
            }
            return ward;
        };
    }

    @Bean
    public ItemWriter<Ward> wards2010Writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Ward> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step stepWards2010(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> wards2010Reader,
                      ItemProcessor<FieldSet, Ward> wards2010Processor, ItemWriter<Ward> wards2010Writer) {
        return stepBuilderFactory.get("wards2010")
                .<FieldSet, Ward> chunk(1000)
                .reader(wards2010Reader)
                .processor(wards2010Processor)
                .writer(wards2010Writer)
                .build();
    }


    @Bean
    public ItemReader<FieldSet> wards2015Reader() {
        FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("obwody-2015.csv"));
        reader.setLinesToSkip(0);
        reader.setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer("|"));
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<FieldSet, Ward> wards2015Processor() {
        ArrayList<Voting> votings = Lists.newArrayList(votingRepository.findByDate(
                Arrays.asList(LocalDate.parse("2015-05-10"), LocalDate.parse("2015-05-24"))
        ));
        return item -> {
            Ward ward = new Ward();
            ward.setVotings(votings);
            ward.setCommunityCode(item.readString(0));
            ward.setWardNo(item.readInt(4));
            ward.setWardAddress(item.readString(8) + " " + " " + item.readString(9) + " " + item.readString(10) + ", " + item.readString(12).replaceFirst(",.*", ""));
            ward.setLabel(item.readString(1).replaceFirst("g?m\\. ", "") + ", Obwodowa Komisja Wyborcza nr " + item.readString(4));
            ward.setShortLabel("Komisja nr " + item.readString(4));
            ward.setVotersCount(item.readInt(13));
            if (item.getFieldCount() >= 14) {
                ward.setLocation(geometryFactory.createPoint(new Coordinate(
                        item.readDouble(15),
                        item.readDouble(14)
                )));
            }
            return ward;
        };
    }

    @Bean
    public ItemWriter<Ward> wards2015Writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Ward> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step stepWards2015(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> wards2015Reader,
                                    ItemProcessor<FieldSet, Ward> wards2015Processor, ItemWriter<Ward> wards2015Writer) {
        return stepBuilderFactory.get("wards2015")
                .<FieldSet, Ward> chunk(1000)
                .reader(wards2015Reader)
                .processor(wards2015Processor)
                .writer(wards2015Writer)
                .build();
    }

    @Bean
    public Job importWardsJob(JobBuilderFactory jobs, Step stepWards2010, Step stepWards2015) {
        return jobs.get("importWards")
                .incrementer(new RunIdIncrementer())
                .flow(stepWards2010).next(stepWards2015)
                .end()
                .build();
    }

}
