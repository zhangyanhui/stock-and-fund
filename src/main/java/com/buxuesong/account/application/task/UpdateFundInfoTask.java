package com.buxuesong.account.application.task;

import com.buxuesong.account.infrastructure.adapter.rest.TiantianFundRestClient;
import com.buxuesong.account.infrastructure.general.service.TencentCloudService;
import com.buxuesong.account.infrastructure.general.utils.DateTimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 基金信息更新任务 定时查询云函数所有用户的基金信息，更新基金净值和收益
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateFundInfoTask {

    @Autowired
    private TencentCloudService tencentCloudService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TiantianFundRestClient tiantianFundRestClient;

    @Value("${TENCENT_CLOUD_ENV_ID:#{null}}")
    private String envId;

    @Value("${TENCENT_CLOUD_TOKEN:#{null}}")
    private String token;

    private static Gson gson = new Gson();

    /**
     * 每日16:30后执行基金信息更新任务 选择在交易日收盘后执行，确保能获取到最新的基金净值
     */
    @Scheduled(cron = "0 30 16 ? * MON-FRI")
    public void updateFundInfo() {
        log.info("======= UpdateFundInfoTask started =======");

        // 检查环境变量是否配置
        if (StringUtils.isEmpty(envId) || StringUtils.isEmpty(token)) {
            log.error("腾讯云环境变量未配置，跳过基金信息更新任务");
            log.error("请设置环境变量：TENCENT_CLOUD_ENV_ID 和 TENCENT_CLOUD_TOKEN");
            return;
        }

        // 检查是否为交易日
        if (!DateTimeUtils.isTradingDay()) {
            log.info("今日非交易日，跳过基金信息更新任务");
            return;
        }

        try {
            // 调用云函数获取所有用户的基金信息
            log.info("调用云函数获取所有用户的基金信息");
            String response = tencentCloudService.callUpdateFundInfo(envId, token, null);
            log.debug("云函数返回结果: {}", response);

            // 解析响应结果
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.has("data") && !rootNode.get("data").isNull()) {
                JsonNode fundDataNode = rootNode.get("data");

                // 遍历所有用户的基金信息
                Iterator<Map.Entry<String, JsonNode>> fields = fundDataNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String openId = entry.getKey();
                    JsonNode userFundsNode = entry.getValue();

                    log.info("处理用户 {} 的基金信息", openId);

                    // 遍历用户的每只基金
                    if (userFundsNode.isArray()) {
                        for (JsonNode fundNode : userFundsNode) {
                            try {
                                // 获取基金基本信息
                                String fundCode = fundNode.get("fundCode").asText();
                                String fundName = fundNode.get("fundName").asText();
                                int fundCount = fundNode.get("fundCount").asInt();
                                BigDecimal fundCost = new BigDecimal(fundNode.get("fundCost").asText());

                                // 查询最新确定净值（实际项目中需要实现获取最新净值的方法）
                                BigDecimal latestNetValue = getLatestConfirmedNetValue(fundCode);

                                // 计算当日收益
                                BigDecimal todayIncome = calculateTodayIncome(fundCount, fundCost, latestNetValue);

                                // 计算总金额
                                BigDecimal totalAmount = latestNetValue.multiply(new BigDecimal(fundCount));

                                // 更新基金用户信息（实际项目中需要实现更新方法）
                                updateFundUserInfo(openId, fundCode, latestNetValue, totalAmount, todayIncome);

                                log.info("基金 {} ({}) 更新完成：最新净值={}, 总金额={}, 当日收益={}",
                                    fundName, fundCode, latestNetValue, totalAmount, todayIncome);
                            } catch (Exception e) {
                                log.error("处理基金信息异常: {}", e.getMessage(), e);
                                continue;
                            }
                        }
                    }
                }
            }

            log.info("======= UpdateFundInfoTask finished =======");
        } catch (Exception e) {
            log.error("更新基金信息任务执行异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取基金最新确定净值 从天天基金API获取最新的基金净值信息
     */
    private BigDecimal getLatestConfirmedNetValue(String fundCode) {
        try {
            log.info("查询基金 {} 最新确定净值", fundCode);

            // 调用天天基金API获取基金信息
            String result = tiantianFundRestClient.getFundInfo(fundCode);

            // 解析响应结果
            if (result != null && !result.equals("jsonpgz();")) {
                String json = result.substring(8, result.length() - 2);
                Map<String, Object> fundData = gson.fromJson(json, Map.class);

                // 获取最新确定净值
                // 注意：实际项目中需要根据API返回的字段结构进行调整
                String dwjz = fundData.getOrDefault("dwjz", "0").toString();
                return new BigDecimal(dwjz);
            }

            log.warn("未能获取到基金 {} 的最新净值，返回默认值", fundCode);
            return new BigDecimal("1.00");
        } catch (Exception e) {
            log.error("获取基金 {} 最新净值异常: {}", fundCode, e.getMessage(), e);
            return new BigDecimal("1.00");
        }
    }

    /**
     * 计算当日收益
     */
    private BigDecimal calculateTodayIncome(int fundCount, BigDecimal fundCost, BigDecimal latestNetValue) {
        // 当日收益 = 持有份额 * (最新净值 - 成本价)
        return latestNetValue.subtract(fundCost).multiply(new BigDecimal(fundCount));
    }

    /**
     * 更新基金用户信息 调用云函数更新用户的基金信息，包括最新净值、总金额和当日收益
     */
    private void updateFundUserInfo(String openId, String fundCode, BigDecimal latestNetValue,
        BigDecimal totalAmount, BigDecimal todayIncome) {
        try {
            // 构建更新参数
            Map<String, Object> payload = new HashMap<>();
            payload.put("openId", openId);
            payload.put("fundCode", fundCode);
            payload.put("unitNetValue", latestNetValue.toString());
            payload.put("fundAmt", totalAmount.toString());
            payload.put("todayAmt", todayIncome.toString());
            payload.put("updateTime", DateTimeUtils.getLocalDateTime());
            payload.put("netValueDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // 调用云函数更新基金用户信息
            log.info("调用云函数更新用户 {} 的基金 {} 信息", openId, fundCode);
            String updateResponse = tencentCloudService.callCloudFunction(envId, "updateFundUserInfo", token, payload);
            log.debug("更新基金用户信息响应: {}", updateResponse);

            // 解析更新响应
            JsonNode updateResult = objectMapper.readTree(updateResponse);
            if (updateResult.has("success") && updateResult.get("success").asBoolean()) {
                log.info("用户 {} 的基金 {} 信息更新成功", openId, fundCode);
            } else {
                log.error("用户 {} 的基金 {} 信息更新失败: {}",
                    openId, fundCode, updateResult.has("message") ? updateResult.get("message").asText() : "未知错误");
            }
        } catch (Exception e) {
            log.error("更新用户 {} 的基金 {} 信息异常: {}", openId, fundCode, e.getMessage(), e);
        }
    }
}