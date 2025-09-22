package com.buxuesong.account.apis.controller;

import com.buxuesong.account.infrastructure.general.service.TencentCloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 腾讯云函数调用控制器
 */
@RestController
@RequestMapping("/api/tencent-cloud")
public class TencentCloudController {

    @Autowired
    private TencentCloudService tencentCloudService;

    /**
     * 调用腾讯云函数
     *
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/function/call")
    public ResponseEntity<String> callFunction(@RequestBody TencentCloudRequest request) {
        String result = tencentCloudService.callCloudFunction(
            request.getEnvId(),
            request.getFunctionName(),
            request.getToken(),
            request.getPayload());

        return ResponseEntity.ok(result);
    }

    /**
     * 请求参数类
     */
    public static class TencentCloudRequest {
        private String envId;
        private String functionName;
        private String token;
        private Map<String, Object> payload;

        // Getters and Setters
        public String getEnvId() {
            return envId;
        }

        public void setEnvId(String envId) {
            this.envId = envId;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Object> payload) {
            this.payload = payload;
        }
    }
}