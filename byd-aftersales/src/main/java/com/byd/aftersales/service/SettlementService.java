package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.SettlementDao;
import com.byd.aftersales.domain.Settlement;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementService {

    private final SettlementDao settlementDao;

    public SettlementService(SettlementDao settlementDao) {
        this.settlementDao = settlementDao;
    }

    public Settlement getByWorkOrderId(Long workOrderId) {
        return settlementDao.findByWorkOrderId(workOrderId)
                .orElseThrow(() -> new BusinessException("结算单不存在，工单可能尚未完工"));
    }

    public Settlement getById(Long settlementId) {
        return settlementDao.findById(settlementId)
                .orElseThrow(() -> new BusinessException("结算单不存在"));
    }

    public List<Settlement> listAll() {
        return settlementDao.findAll();
    }

    public Settlement markPaid(Long settlementId) {
        Settlement s = getById(settlementId);
        if (!"UNPAID".equals(s.getPaymentStatus())) {
            throw new BusinessException("该结算单已支付或已退款");
        }
        if (!"APPROVED".equals(s.getManagerStatus())) {
            throw new BusinessException("结算单尚未通过经理审核，无法收款");
        }
        if (settlementDao.markPaid(settlementId) == 0) {
            throw new BusinessException("支付确认失败，请重试");
        }
        return settlementDao.findById(settlementId).orElseThrow(() -> new BusinessException("结算单不存在"));
    }

    public Settlement approve(Long settlementId, Long operatorId) {
        getById(settlementId);
        if (operatorId == null) {
            throw new BusinessException("操作人不能为空");
        }
        if (settlementDao.approve(settlementId, operatorId) == 0) {
            throw new BusinessException("审核失败，结算单可能已处理");
        }
        return settlementDao.findById(settlementId).orElseThrow(() -> new BusinessException("结算单不存在"));
    }

    public Settlement reject(Long settlementId, Long operatorId) {
        getById(settlementId);
        if (operatorId == null) {
            throw new BusinessException("操作人不能为空");
        }
        if (settlementDao.reject(settlementId, operatorId) == 0) {
            throw new BusinessException("驳回失败，结算单可能已处理");
        }
        return settlementDao.findById(settlementId).orElseThrow(() -> new BusinessException("结算单不存在"));
    }
}
