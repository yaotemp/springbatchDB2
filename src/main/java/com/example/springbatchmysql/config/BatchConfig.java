package com.example.springbatchmysql.config;

import com.example.springbatchmysql.model.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Value("${output.csv.path}")
    private String outputCsvPath;

    @Bean
    public JdbcCursorItemReader<User> reader() {
        JdbcCursorItemReader<User> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT ID, AGE, BIRTHDAY, COUNTRY, EMAIL, FIRST_NAME, GENDER, LAST_NAME, PERSON_ID FROM USERS");
        reader.setRowMapper(new BeanPropertyRowMapper<>(User.class));
        return reader;
    }

    @Bean
    public FlatFileItemWriter<User> writer() {
        FlatFileItemWriter<User> writer = new FlatFileItemWriter<>();
        
        // Get current working directory
        String currentDir = System.getProperty("user.dir");
        
        // Create output directory if it doesn't exist
        File outputDir = new File(currentDir, "output");
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            System.out.println("Output directory created: " + created + " at " + outputDir.getAbsolutePath());
        } else {
            System.out.println("Output directory already exists at: " + outputDir.getAbsolutePath());
        }
        
        // Create output file with absolute path
        File outputFile = new File(outputDir, "users_output.csv");
        System.out.println("Output file will be created at: " + outputFile.getAbsolutePath());
        
        // Use FileSystemResource with absolute path
        writer.setResource(new FileSystemResource(outputFile));
        writer.setAppendAllowed(false);

        DelimitedLineAggregator<User> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<User> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "age", "birthday", "country", "email", "firstName", "gender", "lastName", "personId"});
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        writer.setHeaderCallback(writer1 -> writer1.write("ID,AGE,BIRTHDAY,COUNTRY,EMAIL,FIRST_NAME,GENDER,LAST_NAME,PERSON_ID"));
        
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<User, User>chunk(10)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Job exportUserJob() {
        return jobBuilderFactory.get("exportUserJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
} 