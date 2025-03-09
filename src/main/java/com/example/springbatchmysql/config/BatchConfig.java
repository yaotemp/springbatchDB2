package com.example.springbatchmysql.config;

import com.example.springbatchmysql.model.CylinderChunkDailyRecord;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.Db2PagingQueryProvider;
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
    @Qualifier("db2DataSource")
    private DataSource db2DataSource;

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

    // Step 1: Thread-safe Reader for CYLINDER_CHUNK_DAILY_RECORDS
    @Bean
    public JdbcPagingItemReader<CylinderChunkDailyRecord> reader() {
        JdbcPagingItemReader<CylinderChunkDailyRecord> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(db2DataSource);
        reader.setPageSize(50);

        Db2PagingQueryProvider queryProvider = new Db2PagingQueryProvider();
        queryProvider.setSelectClause("ID, VOLUME_SERIAL, RECORD_DATE, RECORD_TIME, FREE_PERCENTAGE, " +
                "FREE_CYLINDER, CYLINDER_THRESHOLD, MATCHED_VOLUMES, VOLUMES_BELOW_THRESHOLD, " +
                "CURRENT_AVAILABLE_CHUNKS, NEXT_1_DAY, NEXT_7_DAYS, NEXT_30_DAYS, " +
                "NEXT_60_DAYS, NEXT_90_DAYS, NEXT_180_DAYS, CREATED_AT, UPDATED_AT");
        queryProvider.setFromClause("CYLINDER_CHUNK_DAILY_RECORDS");
        queryProvider.setSortKeys(Collections.singletonMap("ID", Order.ASCENDING));
        
        reader.setQueryProvider(queryProvider);
        reader.setRowMapper(new BeanPropertyRowMapper<>(CylinderChunkDailyRecord.class));
        return reader;
    }

    // Step 2: CSV writer configuration
    @Bean
    public FlatFileItemWriter<CylinderChunkDailyRecord> flatFileItemWriter() {
        FlatFileItemWriter<CylinderChunkDailyRecord> writer = new FlatFileItemWriter<>();
        File outputFile = new File(outputCsvPath);
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        writer.setResource(new FileSystemResource(outputFile));
        writer.setAppendAllowed(true);

        DelimitedLineAggregator<CylinderChunkDailyRecord> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CylinderChunkDailyRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
            "id", "volumeSerial", "recordDate", "recordTime", "freePercentage", 
            "freeCylinder", "cylinderThreshold", "matchedVolumes", "volumesBelowThreshold",
            "currentAvailableChunks", "next1Day", "next7Days", "next30Days",
            "next60Days", "next90Days", "next180Days", "createdAt", "updatedAt"
        });
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        return writer;
    }

    // Step 3: Thread-safe Writer
    @Bean
    public SynchronizedItemStreamWriter<CylinderChunkDailyRecord> writer() {
        SynchronizedItemStreamWriter<CylinderChunkDailyRecord> synchronizedWriter = new SynchronizedItemStreamWriter<>();
        synchronizedWriter.setDelegate(flatFileItemWriter());
        return synchronizedWriter;
    }

    // Step 4: CSV header writer listener
    @Bean
    public StepExecutionListener headerWriterListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                try (FileWriter writer = new FileWriter(outputCsvPath, false)) {
                    writer.write("ID,VOLUME_SERIAL,RECORD_DATE,RECORD_TIME,FREE_PERCENTAGE," +
                            "FREE_CYLINDER,CYLINDER_THRESHOLD,MATCHED_VOLUMES,VOLUMES_BELOW_THRESHOLD," +
                            "CURRENT_AVAILABLE_CHUNKS,NEXT_1_DAY,NEXT_7_DAYS,NEXT_30_DAYS," +
                            "NEXT_60_DAYS,NEXT_90_DAYS,NEXT_180_DAYS,CREATED_AT,UPDATED_AT\n");
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

    // Step 5: Multi-threaded Step configuration
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<CylinderChunkDailyRecord, CylinderChunkDailyRecord>chunk(50)
                .reader(reader())
                .writer(writer())
                .listener(headerWriterListener())
                .taskExecutor(batchTaskExecutor())
                .throttleLimit(16)
                .build();
    }

    @Bean
    public Job exportCylinderChunkJob() {
        return jobBuilderFactory.get("exportCylinderChunkJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
}