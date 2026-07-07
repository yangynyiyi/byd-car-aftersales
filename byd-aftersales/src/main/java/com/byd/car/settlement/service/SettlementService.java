package com.byd.car.settlement.service;

import com.byd.car.settlement.entity.Settlement;

public interface SettlementService {

    Settlement getByWorkOrderId(Long workOrderId);

    Settlement getById(Long settlementId);

    Settlement markPaid(Long settlementId);
}
