package com.byd.aftersales.dto;

import java.math.BigDecimal;

public class PartCreateRequest {

    private String partNo;
    private String partName;
    private String category;
    private Integer stockQuantity;
    private Integer warningThreshold;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;

    public String getPartNo() { return partNo; }
    public void setPartNo(String partNo) { this.partNo = partNo; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(Integer warningThreshold) { this.warningThreshold = warningThreshold; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
}
