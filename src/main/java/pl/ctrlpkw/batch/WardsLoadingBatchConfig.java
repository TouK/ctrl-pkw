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
        reader.setResource(new ClassPathResource("obwody-2014-11-15-18-00-27.csv"));
        reader.setEncoding("Cp1250");
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";"));
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
            ward.setCommunityCode(item.readString(2).substring(2, 8));
            ward.setWardNo(item.readInt(5));
            ward.setWardAddress(item.readString(6));
            ward.setLabel("Obwodowa komisja wyborcza: " + item.readString(3).split(",", 2)[0] + " nr " + item.readString(5));
            ward.setShortLabel("Komisja nr " + item.readString(5));
            if (item.getFieldCount() >= 12) {
                ward.setLocation(geometryFactory.createPoint(new Coordinate(
                        item.readDouble(11),
                        item.readDouble(10)
                )));
                if (ward.getLocation().getY() < 49.00 || ward.getLocation().getY() > 54.83 || ward.getLocation().getX() < 14.12 || ward.getLocation().getX() > 24.13) {
                    log.warn("Invalid coordinates of {} {} : {},{}", ward.getCommunityCode(), ward.getWardNo(), ward.getLocation().getY(), ward.getLocation().getX());
                    ward.setLocation(null);
                }
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
    public ItemReader<FieldSet> wards2015AbroadReader() {
        FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("obwody_zagraniczne.csv"));
        reader.setLinesToSkip(0);
        reader.setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";"));
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<FieldSet, Ward> wards2015AbroadProcessor() {
        ArrayList<Voting> votings = Lists.newArrayList(votingRepository.findByDate(
                Arrays.asList(LocalDate.parse("2015-05-10"), LocalDate.parse("2015-05-24"))
        ));
        return item -> {
            Ward ward = new Ward();
            ward.setVotings(votings);
            ward.setCommunityCode("99999");
            ward.setWardNo(item.readInt(0));
            ward.setWardAddress(item.readString(3) + " " + item.readString(4));
            ward.setLabel("Obwodowa komisja wyborcza za granicą nr " + item.readString(0) + " : " + item.readString(2));
            ward.setShortLabel("Komisja nr " + item.readString(0));
            if (item.getFieldCount() >= 7) {
                ward.setLocation(geometryFactory.createPoint(new Coordinate(
                        item.readDouble(6),
                        item.readDouble(5)
                )));
            }
            return ward;
        };
    }

    @Bean
    public Step stepWards2015Abroad(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> wards2015AbroadReader,
                              ItemProcessor<FieldSet, Ward> wards2015AbroadProcessor, ItemWriter<Ward> wards2015Writer) {
        return stepBuilderFactory.get("wards2015Abroad")
                .<FieldSet, Ward> chunk(1000)
                .reader(wards2015AbroadReader)
                .processor(wards2015AbroadProcessor)
                .writer(wards2015Writer)
                .build();
    }



    @Bean
    public Job importWardsJob(JobBuilderFactory jobs, Step stepWards2010, Step stepWards2015, Step stepWards2015Abroad) {
        return jobs.get("importWards")
                .incrementer(new RunIdIncrementer())
                .flow(stepWards2010).next(stepWards2015Abroad).next(stepWards2015)
                .end()
                .build();
    }

}
