package com.example.springbatchmysql.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

@Data
public class CylinderChunkDailyRecord {
    private Long id;
    private String volumeSerial;
    private Date recordDate;
    private Time recordTime;
    private BigDecimal freePercentage;
    private Integer freeCylinder;
    private Integer cylinderThreshold;
    private Integer matchedVolumes;
    private Integer volumesBelowThreshold;
    private Integer currentAvailableChunks;
    private Integer next1Day;
    private Integer next7Days;
    private Integer next30Days;
    private Integer next60Days;
    private Integer next90Days;
    private Integer next180Days;
    private Date createdAt;
    private Date updatedAt;
} 