package com.buxuesong.account.infrastructure.general.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云函数调用服务
 */
@Service
public class TencentCloudService {

    private static final Logger logger = LoggerFactory.getLogger(TencentCloudService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 调用腾讯云函数
     *
     * @param envId        环境ID
     * @param functionName 函数名称
     * @param token        认证令牌
     * @param payload      请求参数
     * @return 响应结果
     */
    public String callCloudFunction(String envId, String functionName, String token, Map<String, Object> payload) {
        try {
            // 构建URL
            String url = String.format("https://%s.api.tcloudbasegateway.com/v1/functions/%s", envId, functionName);

            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);

            // 如果payload为空，创建空对象
            if (payload == null) {
                payload = new HashMap<>();
            }

            // 转换为JSON字符串
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // 创建HTTP实体
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            logger.info("调用腾讯云函数: {}", url);
            logger.debug("请求参数: {}", jsonPayload);

            // 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

            String responseBody = response.getBody();
            logger.info("响应状态: {}", response.getStatusCode());
            logger.debug("响应内容: {}", responseBody);

            return responseBody;

        } catch (Exception e) {
            logger.error("调用腾讯云函数失败", e);
            return String.format("{\"error\": \"%s\"}", e.getMessage());
        }
    }

    /**
     * 调用updateFundInfo云函数
     *
     * @param envId  环境ID
     * @param token  认证令牌
     * @param openId 用户openId（可选）
     * @return 响应结果
     */
    public String callUpdateFundInfo(String envId, String token, String openId) {
        Map<String, Object> payload = new HashMap<>();
        if (openId != null && !openId.isEmpty()) {
            payload.put("openId", openId);
        }
        return callCloudFunction(envId, "updateFundInfo", token, payload);
    }

    /**
     * 调用腾讯云函数（使用空参数）
     *
     * @param envId        环境ID
     * @param functionName 函数名称
     * @param token        认证令牌
     * @return 响应结果
     */
    public String callCloudFunction(String envId, String functionName, String token) {
        return callCloudFunction(envId, functionName, token, null);
    }
}