package com.buxuesong.account.infrastructure.general.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP客户端工具类
 */
public class HttpClientUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送POST请求到腾讯云函数
     *
     * @param envId        环境ID
     * @param functionName 函数名称
     * @param token        认证令牌
     * @return 响应结果
     */
    public static String callTencentCloudFunction(String envId, String functionName, String token) {
        try {
            // 构建URL
            String url = "https://" + envId + ".api.tcloudbasegateway.com/v1/functions/" + functionName;

            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + token);

            // 创建空的JSON payload
            Map<String, Object> payload = new HashMap<>();
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // 创建HTTP实体
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            // 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 示例用法
     */
    public static void main(String[] args) {
        String envId = "your-envId";
        String functionName = "your-function-name";
        String token = "your-token";

        String result = callTencentCloudFunction(envId, functionName, token);
        System.out.println(result);
    }
}