package com.example.springbatchmysql.config;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;

import javax.sql.DataSource;
@Configuration
public class DataSourceConfig {

    // ========== H2 数据源（Spring Batch 元数据）==========
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.h2")
    public DataSourceProperties h2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @Qualifier("batchDataSource")
    public DataSource batchDataSource() {
        return h2DataSourceProperties().initializeDataSourceBuilder().build();
    }

    // ========== MySQL 数据源（业务数据）==========
    @Bean
    @ConfigurationProperties("app.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Qualifier("mysqlDataSource")
    public DataSource mysqlDataSource() {
        return mysqlDataSourceProperties().initializeDataSourceBuilder().build();
    }
    
    // Add a BatchConfigurer to explicitly set which DataSource to use for Spring Batch
    @Bean
    public BatchConfigurer batchConfigurer(@Qualifier("batchDataSource") DataSource dataSource) {
        return new DefaultBatchConfigurer(dataSource);
    }
}