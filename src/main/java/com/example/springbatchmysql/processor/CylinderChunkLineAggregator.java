package com.example.springbatchmysql.processor;

import com.example.springbatchmysql.model.CylinderChunkDailyRecord;
import org.springframework.batch.item.file.transform.LineAggregator;
import java.util.Base64;
import java.text.SimpleDateFormat;

public class CylinderChunkLineAggregator implements LineAggregator<CylinderChunkDailyRecord> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String aggregate(CylinderChunkDailyRecord record) {
        StringBuilder sb = new StringBuilder();
        
        // Add basic fields
        sb.append(record.getId()).append(",");
        sb.append(escapeField(record.getVolumeSerial())).append(",");
        sb.append(record.getRecordDate() != null ? dateFormat.format(record.getRecordDate()) : "").append(",");
        sb.append(record.getRecordTime() != null ? timeFormat.format(record.getRecordTime()) : "").append(",");
        sb.append(record.getFreePercentage()).append(",");
        sb.append(record.getFreeCylinder()).append(",");
        sb.append(record.getCylinderThreshold()).append(",");
        sb.append(record.getMatchedVolumes()).append(",");
        sb.append(record.getVolumesBelowThreshold()).append(",");
        sb.append(record.getCurrentAvailableChunks()).append(",");
        sb.append(record.getNext1Day()).append(",");
        sb.append(record.getNext7Days()).append(",");
        sb.append(record.getNext30Days()).append(",");
        sb.append(record.getNext60Days()).append(",");
        sb.append(record.getNext90Days()).append(",");
        sb.append(record.getNext180Days()).append(",");
        sb.append(record.getCreatedAt() != null ? timestampFormat.format(record.getCreatedAt()) : "").append(",");
        sb.append(record.getUpdatedAt() != null ? timestampFormat.format(record.getUpdatedAt()) : "").append(",");
        
        // Handle BINARY_DATA - convert to Base64
        if (record.getBinaryData() != null && record.getBinaryData().length > 0) {
            sb.append(Base64.getEncoder().encodeToString(record.getBinaryData()));
        }
        sb.append(",");
        
        // Handle TEXT_DATA
        if (record.getTextData() != null) {
            sb.append(escapeField(record.getTextData()));
        }
        
        return sb.toString();
    }
    
    // Escape special characters in CSV fields
    private String escapeField(String field) {
        if (field == null) {
            return "";
        }
        
        // If field contains commas, quotes, or newlines, wrap in quotes and escape internal quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 