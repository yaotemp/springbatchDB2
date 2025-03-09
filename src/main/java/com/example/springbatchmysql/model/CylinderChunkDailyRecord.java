package com.example.springbatchmysql.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
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
    
    // 添加 BLOB 和 CLOB 字段
    private byte[] binaryData;  // 用于存储 BLOB 数据
    private String textData;    // 用于存储 CLOB 数据
} 