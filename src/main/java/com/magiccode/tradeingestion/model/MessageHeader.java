package com.magiccode.tradeingestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MessageHeader {
    @Column(name = "message_id")
    private String messageId;
    
    @Column(name = "message_type")
    private String messageType;
    
    @Column(name = "schema_version")
    private String schemaVersion;
    
    @Column(name = "message_format")
    private String messageFormat;
    
    @Column(name = "created_timestamp")
    private Instant createdTimestamp;
    
    @Column(name = "source_system")
    private String sourceSystem;
    
    @Column(name = "destination_system")
    private String destinationSystem;
    
    @Column(name = "sensitivity_level")
    private String sensitivityLevel;
    
    @Embedded
    private SecurityTags securityTags;
    
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityTags {
        @Column(name = "encryption")
        private String encryption;
        
        @Column(name = "mask_pii")
        private boolean maskPII;
        
        @Column(name = "data_classification")
        private String dataClassification;
    }
} 