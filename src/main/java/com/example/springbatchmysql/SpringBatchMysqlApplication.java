package com.example.springbatchmysql;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringBatchMysqlApplication implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("exportCylinderChunkJob")
    private Job exportCylinderChunkJob;

    public static void main(String[] args) {
        // Store the context so we can close it after the job completes
        ConfigurableApplicationContext ctx = SpringApplication.run(SpringBatchMysqlApplication.class, args);
        
        // Exit code 0 means successful completion
        System.exit(0);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create job parameters with a timestamp to make each run unique
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        
        // Run the job
        jobLauncher.run(exportCylinderChunkJob, jobParameters);
    }
} 