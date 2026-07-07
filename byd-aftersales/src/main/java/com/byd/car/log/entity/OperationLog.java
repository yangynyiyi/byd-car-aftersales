package com.byd.car.log.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "operation_log")
public class OperationLog {

    @Id
    private String id;

    /**
     * WORK_ORDER / SETTLEMENT / PART / PART_USAGE
     */
    private String businessType;

    private Long businessId;

    private String action;

    private Long operatorId;

    private String detail;

    private LocalDateTime createdAt;
}
