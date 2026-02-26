package com.buxuesong.account.infrastructure.general.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 基金信息实体类
 */
public class FundInfo {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("openId")
    private String openId;

    @JsonProperty("fundName")
    private String fundName;

    @JsonProperty("fundCode")
    private String fundCode;

    @JsonProperty("fundAmt")
    private BigDecimal fundAmt;

    @JsonProperty("holdingProfit")
    private BigDecimal holdingProfit;

    @JsonProperty("fundCount")
    private int fundCount;

    @JsonProperty("todayAmt")
    private BigDecimal todayAmt;

    @JsonProperty("unitNetValue")
    private BigDecimal unitNetValue;

    @JsonProperty("netValueDate")
    private String netValueDate;

    @JsonProperty("fundCost")
    private BigDecimal fundCost;

    @JsonProperty("importDate")
    private String importDate;

    @JsonProperty("createTime")
    private String createTime;

    @JsonProperty("updateTime")
    private String updateTime;

    // 构造函数
    public FundInfo() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public BigDecimal getFundAmt() {
        return fundAmt;
    }

    public void setFundAmt(BigDecimal fundAmt) {
        this.fundAmt = fundAmt;
    }

    public BigDecimal getHoldingProfit() {
        return holdingProfit;
    }

    public void setHoldingProfit(BigDecimal holdingProfit) {
        this.holdingProfit = holdingProfit;
    }

    public int getFundCount() {
        return fundCount;
    }

    public void setFundCount(int fundCount) {
        this.fundCount = fundCount;
    }

    public BigDecimal getTodayAmt() {
        return todayAmt;
    }

    public void setTodayAmt(BigDecimal todayAmt) {
        this.todayAmt = todayAmt;
    }

    public BigDecimal getUnitNetValue() {
        return unitNetValue;
    }

    public void setUnitNetValue(BigDecimal unitNetValue) {
        this.unitNetValue = unitNetValue;
    }

    public String getNetValueDate() {
        return netValueDate;
    }

    public void setNetValueDate(String netValueDate) {
        this.netValueDate = netValueDate;
    }

    public BigDecimal getFundCost() {
        return fundCost;
    }

    public void setFundCost(BigDecimal fundCost) {
        this.fundCost = fundCost;
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 计算收益率
     */
    public BigDecimal getProfitRate() {
        if (fundAmt != null && holdingProfit != null && fundAmt.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cost = fundAmt.subtract(holdingProfit);
            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                return holdingProfit.divide(cost, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "FundInfo{" +
            "id='" + id + '\'' +
            ", fundName='" + fundName + '\'' +
            ", fundCode='" + fundCode + '\'' +
            ", fundAmt=" + fundAmt +
            ", holdingProfit=" + holdingProfit +
            '}';
    }
}