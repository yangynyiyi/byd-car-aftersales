package com.byd.car.log.repository;

import com.byd.car.log.entity.OperationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OperationLogRepository extends MongoRepository<OperationLog, String> {

    List<OperationLog> findByBusinessTypeAndBusinessIdOrderByCreatedAtDesc(
            String businessType, Long businessId);
}
