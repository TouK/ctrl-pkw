package pl.ctrlpkw.batch;

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
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

@Slf4j
@Configuration
public class WardsLoadingBatchConfig {

    @Resource
    private GeometryFactory geometryFactory;

    @Resource
    private VotingRepository votingRepository;

    @Bean
    public ItemReader<FieldSet> reader() {
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
    public ItemProcessor<FieldSet, Ward> processor() {
        return item -> {
            Ward ward = new Ward();
            ward.setVoting(votingRepository.findByDate(LocalDate.parse("2010-06-20")));
            ward.setCommunityCode(item.readString(2));
            ward.setWardNo(item.readInt(6));
            ward.setWardAddress(item.readString(7));
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
    public ItemWriter<Ward> writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Ward> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job importWardsJob(JobBuilderFactory jobs, Step s1) {
        return jobs.get("importWards")
                .incrementer(new RunIdIncrementer())
                .flow(s1)
                .end()
                .build();
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<FieldSet> reader,
                      ItemProcessor<FieldSet, Ward> processor, ItemWriter<Ward> writer) {
        return stepBuilderFactory.get("step1")
                .<FieldSet, Ward> chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


}
