package com.example.springbatchmysql.mapper;

import com.example.springbatchmysql.model.CylinderChunkDailyRecord;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CylinderChunkRowMapper implements RowMapper<CylinderChunkDailyRecord> {

    @Override
    public CylinderChunkDailyRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        CylinderChunkDailyRecord record = new CylinderChunkDailyRecord();
        
        // Map basic fields
        record.setId(rs.getLong("ID"));
        record.setVolumeSerial(rs.getString("VOLUME_SERIAL"));
        record.setRecordDate(rs.getDate("RECORD_DATE"));
        record.setRecordTime(rs.getTime("RECORD_TIME"));
        record.setFreePercentage(rs.getBigDecimal("FREE_PERCENTAGE"));
        record.setFreeCylinder(rs.getInt("FREE_CYLINDER"));
        record.setCylinderThreshold(rs.getInt("CYLINDER_THRESHOLD"));
        record.setMatchedVolumes(rs.getInt("MATCHED_VOLUMES"));
        record.setVolumesBelowThreshold(rs.getInt("VOLUMES_BELOW_THRESHOLD"));
        record.setCurrentAvailableChunks(rs.getInt("CURRENT_AVAILABLE_CHUNKS"));
        record.setNext1Day(rs.getInt("NEXT_1_DAY"));
        record.setNext7Days(rs.getInt("NEXT_7_DAYS"));
        record.setNext30Days(rs.getInt("NEXT_30_DAYS"));
        record.setNext60Days(rs.getInt("NEXT_60_DAYS"));
        record.setNext90Days(rs.getInt("NEXT_90_DAYS"));
        record.setNext180Days(rs.getInt("NEXT_180_DAYS"));
        record.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        record.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        
        // Handle BINARY_DATA (BLOB)
        Blob binaryBlob = rs.getBlob("BINARY_DATA");
        if (binaryBlob != null && !rs.wasNull()) {
            long blobLength = binaryBlob.length();
            if (blobLength > 0) {
                record.setBinaryData(binaryBlob.getBytes(1, (int) blobLength));
            }
            binaryBlob.free();
        }
        
        // Handle TEXT_DATA (CLOB or TEXT in MySQL)
        record.setTextData(rs.getString("TEXT_DATA"));
        
        return record;
    }
} 