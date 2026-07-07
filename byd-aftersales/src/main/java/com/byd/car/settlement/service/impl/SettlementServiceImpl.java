package com.byd.car.settlement.service.impl;

import com.byd.car.common.exception.BusinessException;
import com.byd.car.settlement.dao.SettlementDao;
import com.byd.car.settlement.entity.Settlement;
import com.byd.car.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementDao settlementDao;

    @Override
    public Settlement getByWorkOrderId(Long workOrderId) {
        Settlement s = settlementDao.findByWorkOrderId(workOrderId);
        if (s == null) {
            throw new BusinessException(404, "结算单不存在，工单可能尚未完工");
        }
        return s;
    }

    @Override
    public Settlement getById(Long settlementId) {
        Settlement s = settlementDao.findById(settlementId);
        if (s == null) {
            throw new BusinessException(404, "结算单不存在");
        }
        return s;
    }

    @Override
    public Settlement markPaid(Long settlementId) {
        Settlement s = getById(settlementId);
        if (!"UNPAID".equals(s.getPaymentStatus())) {
            throw new BusinessException("该结算单已支付或已退款");
        }
        int rows = settlementDao.markPaid(settlementId);
        if (rows == 0) {
            throw new BusinessException("支付确认失败，请重试");
        }
        return settlementDao.findById(settlementId);
    }
}
