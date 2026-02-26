package com.buxuesong.account.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundPO {
    // 基础字段
    private String code;
    private String name;
    private BigDecimal costPrise;
    private String bonds;
    private String app;
    private boolean hide;

    // 前端需要的字段（为了兼容性，同时保留原字段名和前端期望的字段名）
    private String fundCode; // 基金代码（前端期望字段名）
    private String fundName; // 基金名称（前端期望字段名）

    // 净值相关字段
    private String jzrq; // 净值日期
    private String dwjz; // 当日净值
    private String gsz; // 估算净值
    private String gszzl; // 估算涨跌百分比
    private String gztime; // 估值时间

    // 收益相关字段
    private String incomePercent; // 收益率
    private String income; // 收益
    private String dayIncome; // 当日收益
    private String marketValue; // 市值

    // 历史涨跌幅字段
    private String oneYearAgoUpper; // 近一年涨跌幅
    private String oneSeasonAgoUpper; // 近一季度涨跌幅
    private String oneMonthAgoUpper; // 近一月涨跌幅
    private String oneWeekAgoUpper; // 近一周涨跌幅

    // 其他字段
    private String currentDayJingzhi; // 当日净值（每个交易日晚9点之后有新日期数据）
    private String previousDayJingzhi; // 前一日净值
}
