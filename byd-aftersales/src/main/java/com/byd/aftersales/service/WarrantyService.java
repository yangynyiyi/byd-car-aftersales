package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.WarrantyEstimate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class WarrantyService {

    private static final int WARRANTY_YEARS = 6;
    private static final BigDecimal WARRANTY_MILEAGE_KM = new BigDecimal("150000");
    private static final Set<String> WEAR_CATEGORIES = Set.of("BRAKE", "TIRE", "BODY");

    private final WorkOrderDao workOrderDao;
    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;
    private final PartUsageDao partUsageDao;
    private final PartDao partDao;

    public WarrantyService(WorkOrderDao workOrderDao,
                           FaultRecordDao faultRecordDao,
                           VehicleDao vehicleDao,
                           PartUsageDao partUsageDao,
                           PartDao partDao) {
        this.workOrderDao = workOrderDao;
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
        this.partUsageDao = partUsageDao;
        this.partDao = partDao;
    }

    public WarrantyEstimate estimateForWorkOrder(Long workOrderId) {
        WorkOrder workOrder = workOrderDao.findById(workOrderId)
                .orElseThrow(() -> new BusinessException("工单不存在"));
        FaultRecord fault = faultRecordDao.findById(workOrder.getFaultId())
                .orElseThrow(() -> new BusinessException("关联故障不存在"));
        Vehicle vehicle = vehicleDao.findByVin(fault.getVin())
                .orElseThrow(() -> new BusinessException("关联车辆不存在"));

        BigDecimal laborAmount = workOrder.getLaborCost() != null ? workOrder.getLaborCost() : BigDecimal.ZERO;
        BigDecimal partAmount = sumApprovedPartAmount(workOrderId);
        BigDecimal grossAmount = laborAmount.add(partAmount);

        List<String> notes = new ArrayList<>();
        boolean inWarranty = isVehicleInWarranty(vehicle, notes);
        BigDecimal eligiblePartAmount = sumEligibleWarrantyPartAmount(workOrderId, inWarranty, notes);

        BigDecimal suggestedWarranty = BigDecimal.ZERO;
        if (inWarranty) {
            // 简化：质保内可减免 = 符合条件的备件费 + 最多 50% 工时
            BigDecimal laborCap = laborAmount.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
            suggestedWarranty = eligiblePartAmount.add(laborCap).min(grossAmount);
            notes.add("建议质保减免已含符合条件的备件费及最多 50% 工时费");
        } else {
            notes.add("车辆已超出简化质保期/里程，建议质保减免为 0");
        }

        BigDecimal customerPayable = grossAmount.subtract(suggestedWarranty).max(BigDecimal.ZERO);

        WarrantyEstimate estimate = new WarrantyEstimate();
        estimate.setLaborAmount(laborAmount);
        estimate.setPartAmount(partAmount);
        estimate.setGrossAmount(grossAmount);
        estimate.setSuggestedWarrantyAmount(suggestedWarranty);
        estimate.setCustomerPayable(customerPayable);
        estimate.setVehicleInWarranty(inWarranty);
        estimate.setVehicleVin(vehicle.getVin());
        if (vehicle.getPurchaseDate() != null) {
            estimate.setVehicleAgeYears(Period.between(vehicle.getPurchaseDate(), LocalDate.now()).getYears());
        }
        estimate.setVehicleMileage(vehicle.getCurrentMileage());
        estimate.setNotes(notes);
        return estimate;
    }

    private boolean isVehicleInWarranty(Vehicle vehicle, List<String> notes) {
        if (vehicle.getPurchaseDate() == null) {
            notes.add("未登记购车日期，按不在质保期处理");
            return false;
        }
        int years = Period.between(vehicle.getPurchaseDate(), LocalDate.now()).getYears();
        if (years >= WARRANTY_YEARS) {
            notes.add("购车已满 " + years + " 年，超出 " + WARRANTY_YEARS + " 年整车质保");
            return false;
        }
        if (vehicle.getCurrentMileage() != null
                && vehicle.getCurrentMileage().compareTo(WARRANTY_MILEAGE_KM) >= 0) {
            notes.add("里程已达 " + vehicle.getCurrentMileage() + " km，超出 " + WARRANTY_MILEAGE_KM + " km");
            return false;
        }
        notes.add("车辆在简化整车质保期内（" + WARRANTY_YEARS + " 年 / " + WARRANTY_MILEAGE_KM + " km）");
        return true;
    }

    private BigDecimal sumApprovedPartAmount(Long workOrderId) {
        return partUsageDao.findApprovedByWorkOrderId(workOrderId).stream()
                .map(u -> u.getUnitPrice().multiply(BigDecimal.valueOf(u.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumEligibleWarrantyPartAmount(Long workOrderId, boolean inWarranty, List<String> notes) {
        if (!inWarranty) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PartUsage usage : partUsageDao.findApprovedByWorkOrderId(workOrderId)) {
            Part part = partDao.findById(usage.getPartId()).orElse(null);
            String category = part != null ? part.getCategory() : "";
            BigDecimal line = usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity()));
            if (WEAR_CATEGORIES.contains(category)) {
                notes.add("备件【" + (part != null ? part.getPartName() : usage.getPartId()) + "】为易损件，不计入质保减免");
                continue;
            }
            total = total.add(line);
        }
        return total;
    }
}
