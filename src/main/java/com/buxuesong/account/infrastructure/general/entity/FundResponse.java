package com.buxuesong.account.infrastructure.general.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 基金数据响应类
 */
public class FundResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("total")
    private int total;

    @JsonProperty("data")
    private List<FundInfo> data;

    // 构造函数
    public FundResponse() {
    }

    public FundResponse(boolean success, int total, List<FundInfo> data) {
        this.success = success;
        this.total = total;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<FundInfo> getData() {
        return data;
    }

    public void setData(List<FundInfo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FundResponse{" +
            "success=" + success +
            ", total=" + total +
            ", data=" + (data != null ? data.size() + " items" : "null") +
            '}';
    }
}