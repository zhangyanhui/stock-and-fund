package com.buxuesong.account.apis.controller;

import com.buxuesong.account.apis.model.response.Response;
import com.buxuesong.account.infrastructure.general.entity.FundInfo;
import com.buxuesong.account.infrastructure.general.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private FundService fundService;

    @Value("${TENCENT_CLOUD_ENV_ID:}")
    private String envId;

    @Value("${TENCENT_CLOUD_TOKEN:}")
    private String token;

    @Value("${TENCENT_CLOUD_FUND_FUNCTION_NAME:getAllUserFunds}")
    private String functionName;

    /**
     * 测试API端点 - 直接调用FundService.getUserFundList方法 该端点用于验证token配置是否正确
     */
    @GetMapping(value = "/test/fund-service")
    public Response testFundService(@RequestParam(value = "openId", required = false) String openId) {
        try {
            log.info("=== 测试FundService开始 ===");
            log.info("测试参数 - openId: {}", openId);

            // 打印环境变量配置
            log.info("环境变量配置:");
            log.info("  envId: {}", envId);
            log.info("  functionName: {}", functionName);
            log.info("  token长度: {}", token != null ? token.length() : 0);
            log.info("  token前10位: {}", token != null && token.length() > 10 ? token.substring(0, 10) + "..." : token);

            // 调用FundService.getUserFundList方法
            log.info("开始调用FundService.getUserFundList方法...");
            List<FundInfo> fundList = fundService.getUserFundList(openId);
            log.info("FundService.getUserFundList方法调用完成，返回结果数量: {}", fundList != null ? fundList.size() : 0);

            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", fundList);
            result.put("total", fundList != null ? fundList.size() : 0);
            result.put("envId", envId);
            result.put("functionName", functionName);
            result.put("tokenConfigured", token != null && !token.isEmpty());

            log.info("=== 测试FundService结束 ===");

            return Response.builder()
                .code("00000000")
                .value(result)
                .build();

        } catch (Exception e) {
            log.error("测试FundService失败", e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("envId", envId);
            errorResult.put("functionName", functionName);
            errorResult.put("tokenConfigured", token != null && !token.isEmpty());

            return Response.builder()
                .code("00000001")
                .msg("测试失败: " + e.getMessage())
                .value(errorResult)
                .build();
        }
    }
}