package com.example.springbatchmysql.config;

import com.example.springbatchmysql.model.User;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;

    @Value("${output.csv.path}")
    private String outputCsvPath;

    // Define a primary TaskExecutor that will be used by Spring Batch
    @Bean
    @Primary
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    // Step 1: Thread-safe Reader
    @Bean
    public JdbcPagingItemReader<User> reader() {
        JdbcPagingItemReader<User> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(mysqlDataSource);
        reader.setPageSize(50);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, age, birthday, country, email, first_name, gender, last_name, person_id");
        queryProvider.setFromClause("users");
        queryProvider.setSortKeys(Collections.singletonMap("id", Order.ASCENDING));

        reader.setQueryProvider(queryProvider);
        reader.setRowMapper(new BeanPropertyRowMapper<>(User.class));
        return reader;
    }

    // Step 2: CSV writer configuration
    @Bean
    public FlatFileItemWriter<User> flatFileItemWriter() {
        FlatFileItemWriter<User> writer = new FlatFileItemWriter<>();
        File outputFile = new File(outputCsvPath);
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        writer.setResource(new FileSystemResource(outputFile));
        writer.setAppendAllowed(true);

        DelimitedLineAggregator<User> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<User> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
            "id", "age", "birthday", "country", "email", 
            "firstName", "gender", "lastName", "personId"
        });
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        return writer;
    }

    // Step 3: Thread-safe Writer
    @Bean
    public SynchronizedItemStreamWriter<User> writer() {
        SynchronizedItemStreamWriter<User> synchronizedWriter = new SynchronizedItemStreamWriter<>();
        synchronizedWriter.setDelegate(flatFileItemWriter());
        return synchronizedWriter;
    }

    // Step 5: CSV header writer listener
    @Bean
    public StepExecutionListener headerWriterListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                try (FileWriter writer = new FileWriter(outputCsvPath, false)) {
                    writer.write("ID,AGE,BIRTHDAY,COUNTRY,EMAIL,FIRST_NAME,GENDER,LAST_NAME,PERSON_ID\n");
                } catch (IOException e) {
                    throw new ItemStreamException("Failed to write CSV header", e);
                }
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return ExitStatus.COMPLETED;
            }
        };
    }

    // Step 6: Multi-threaded Step configuration
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<User, User>chunk(50)
                .reader(reader())
                .writer(writer())
                .listener(headerWriterListener())
                .taskExecutor(batchTaskExecutor())
                .throttleLimit(16)
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