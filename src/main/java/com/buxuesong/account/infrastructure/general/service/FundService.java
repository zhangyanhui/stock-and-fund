package com.buxuesong.account.infrastructure.general.service;

import com.buxuesong.account.infrastructure.general.entity.FundInfo;
import com.buxuesong.account.infrastructure.general.entity.FundResponse;
import com.buxuesong.account.infrastructure.general.util.HttpClientUtil;
import com.buxuesong.account.infrastructure.persistent.po.FundPO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基金服务类
 */
@Service
public class FundService {

    private static final Logger logger = LoggerFactory.getLogger(FundService.class);

    @Autowired
    private TencentCloudService tencentCloudService;

    @Autowired
    private ObjectMapper objectMapper;

    // 配置参数，可以在application.properties中配置
    @Value("${tencent.cloud.envId:spring-3go98zd4f98e1fb9}")
    private String envId;

    @Value("${tencent.cloud.fund.functionName:getAllUserFunds}")
    private String functionName;

    @Value("${tencent.cloud.token:eyJhbGciOiJSUzI1NiIsImtpZCI6IjlkMWRjMzFlLWI0ZDAtNDQ4Yi1hNzZmLWIwY2M2M2Q4MTQ5OCJ9.eyJhdWQiOiJzcHJpbmctM2dvOTh6ZDRmOThlMWZiOSIsImV4cCI6MjUzNDAyMzAwNzk5LCJpYXQiOjE3NTgyNDgwMTEsImF0X2hhc2giOiJQTnhnMXBULUVmQ083VkpVQU5namh3IiwicHJvamVjdF9pZCI6InNwcmluZy0zZ285OHpkNGY5OGUxZmI5IiwibWV0YSI6eyJwbGF0Zm9ybSI6IndlYiIsImVudiI6InNwcmluZy0zZ285OHpkNGY5OGUxZmI5IiwiZW52X2lkIjoic3ByaW5nLTNnbzk4emQ0Zjk4ZTFmYjkifX0.}")
    private String token;

    /**
     * 获取用户基金列表
     *
     * @param openId 用户openId
     * @return 基金列表
     */
    public List<FundInfo> getUserFundList(String openId) {
        try {
            // 构建请求参数
            Map<String, Object> payload = new HashMap<>();
            if (openId != null && !openId.isEmpty()) {
                payload.put("openId", openId);
            }
            // 打印请求参数
            logger.info("调用云函数获取用户基金列表，参数: {}", payload);
            // 打印envId
            logger.info("envId: {}", envId);
            // 打印functionName
            logger.info("functionName: {}", functionName);
            // 打印token
            logger.info("token: {}", token);

            // 调用云函数
//            String responseJson = tencentCloudService.callCloudFunction(envId, functionName, token, payload);
//// 使用示例
//            String result = HttpClientUtil.callTencentCloudFunction(envId, functionName, token);
//            System.out.println(result);
            String responseJson = HttpClientUtil.callTencentCloudFunction(envId, functionName, token);
            //
            logger.info("responseJson: {}", responseJson);

            if (responseJson == null || responseJson.isEmpty()) {
                logger.warn("云函数返回空响应");
                return new ArrayList<>();
            }

            // 解析响应
            FundResponse fundResponse = objectMapper.readValue(responseJson, FundResponse.class);

            if (fundResponse.isSuccess() && fundResponse.getData() != null) {
                logger.info("成功获取基金数据，共{}条记录", fundResponse.getTotal());
                return fundResponse.getData();
            } else {
                logger.warn("云函数返回失败或无数据: {}", fundResponse);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("获取基金列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有基金列表（不过滤用户）
     *
     * @return 基金列表
     */
    public List<FundInfo> getAllFundList() {
        return getUserFundList(null);
    }

    /**
     * 根据基金代码获取基金信息
     *
     * @param fundCode 基金代码
     * @return 基金信息
     */
    public FundInfo getFundByCode(String fundCode) {
        List<FundInfo> fundList = getAllFundList();
        return fundList.stream()
            .filter(fund -> fundCode.equals(fund.getFundCode()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取用户基金列表（返回FundPO格式）
     *
     * @param openId 用户openId
     * @return 基金列表（FundPO格式）
     */
    public List<String> getUserFundListAsPO(String openId) {
        List<FundInfo> fundInfoList = getUserFundList(openId);
        List list = new ArrayList();
        if (!CollectionUtils.isEmpty(fundInfoList)) {
            for (FundInfo fundInfo : fundInfoList) {
                String fundArr = fundInfo.getFundCode() + "," + fundInfo.getFundCost() + "," + fundInfo.getFundCount() + ","
                    + fundInfo.getOpenId();

                list.add(fundArr);
            }

        }
        return list;
    }

    /**
     * 将FundInfo转换为FundPO
     *
     * @param fundInfo 云函数返回的基金信息
     * @return FundPO对象
     */
    private FundPO convertFundInfoToFundPO(FundInfo fundInfo) {
        return FundPO.builder()
            // 基础字段
            .code(fundInfo.getFundCode())
            .name(fundInfo.getFundName())
            .costPrise(fundInfo.getFundCost() != null ? fundInfo.getFundCost() : BigDecimal.ZERO)
            .bonds(fundInfo.getFundCount() != 0 ? String.valueOf(fundInfo.getFundCount()) : "0")
            .app("云函数") // 标识数据来源
            .hide(false)

            // 前端期望的字段名（兼容性）
            .fundCode(fundInfo.getFundCode())
            .fundName(fundInfo.getFundName())

            // 净值相关字段（从FundInfo获取或设置默认值）
            .jzrq(fundInfo.getNetValueDate() != null ? fundInfo.getNetValueDate() : "")
            .dwjz(fundInfo.getUnitNetValue() != null ? fundInfo.getUnitNetValue().toString() : "0")
            .gsz(fundInfo.getUnitNetValue() != null ? fundInfo.getUnitNetValue().toString() : "0") // 估算净值暂用单位净值
            .gszzl("0.00") // 估算涨跌百分比，暂设为0
            .gztime(fundInfo.getNetValueDate() != null ? fundInfo.getNetValueDate() : "")

            // 收益相关字段
            .incomePercent(calculateIncomePercent(fundInfo))
            .income(fundInfo.getHoldingProfit() != null ? fundInfo.getHoldingProfit().toString() : "0")
            .dayIncome("0") // 当日收益，暂设为0
            .marketValue(calculateMarketValue(fundInfo))

            // 历史涨跌幅字段（暂设为0）
            .oneYearAgoUpper("0.00")
            .oneSeasonAgoUpper("0.00")
            .oneMonthAgoUpper("0.00")
            .oneWeekAgoUpper("0.00")

            // 其他字段
            .currentDayJingzhi(fundInfo.getUnitNetValue() != null ? fundInfo.getUnitNetValue().toString() : "0")
            .previousDayJingzhi("0")
            .build();
    }

    /**
     * 计算收益率
     */
    private String calculateIncomePercent(FundInfo fundInfo) {
        if (fundInfo.getHoldingProfit() != null && fundInfo.getFundAmt() != null &&
            fundInfo.getFundAmt().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costValue = fundInfo.getFundAmt().subtract(fundInfo.getHoldingProfit());
            if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = fundInfo.getHoldingProfit()
                    .divide(costValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                return percent.toString();
            }
        }
        return "0.00";
    }

    /**
     * 计算市值
     */
    private String calculateMarketValue(FundInfo fundInfo) {
        if (fundInfo.getFundAmt() != null) {
            return fundInfo.getFundAmt().toString();
        }
        return "0";
    }

    /**
     * 获取所有基金列表（返回FundPO格式，不过滤用户）
     *
     * @return 基金列表（FundPO格式）
     */
    public List<String> getAllFundListAsPO() {
        return getUserFundListAsPO(null);
    }

    /**
     * 获取基金统计信息
     *
     * @param openId 用户openId
     * @return 统计信息
     */
    public Map<String, Object> getFundStatistics(String openId) {
        List<FundInfo> fundList = getUserFundList(openId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", fundList.size());

        if (!fundList.isEmpty()) {
            double totalAmt = fundList.stream()
                .mapToDouble(fund -> fund.getFundAmt() != null ? fund.getFundAmt().doubleValue() : 0.0)
                .sum();

            double totalProfit = fundList.stream()
                .mapToDouble(fund -> fund.getHoldingProfit() != null ? fund.getHoldingProfit().doubleValue() : 0.0)
                .sum();

            statistics.put("totalAmt", totalAmt);
            statistics.put("totalProfit", totalProfit);
            statistics.put("profitRate", totalAmt > 0 ? (totalProfit / (totalAmt - totalProfit)) * 100 : 0.0);
        } else {
            statistics.put("totalAmt", 0.0);
            statistics.put("totalProfit", 0.0);
            statistics.put("profitRate", 0.0);
        }

        return statistics;
    }
}