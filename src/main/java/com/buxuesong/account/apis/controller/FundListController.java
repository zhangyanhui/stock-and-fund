package com.buxuesong.account.apis.controller;

import com.buxuesong.account.apis.model.response.Response;
import com.buxuesong.account.infrastructure.general.service.TencentCloudService;
import com.buxuesong.account.domain.model.fund.FundEntity;
import com.buxuesong.account.infrastructure.adapter.rest.SinaRestClient;
import com.buxuesong.account.infrastructure.adapter.rest.TiantianFundRestClient;
import com.buxuesong.account.infrastructure.general.entity.FundInfo;
import com.buxuesong.account.infrastructure.general.service.FundService;
import com.buxuesong.account.infrastructure.general.utils.UserUtils;
import com.buxuesong.account.infrastructure.persistent.po.FundPO;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 基金列表控制器
 */
@Controller
@RequestMapping("/fund")
@Slf4j
public class FundListController {

    @Autowired
    private FundService fundService;
    @Autowired
    private FundEntity fundEntity;
    @Autowired
    private TencentCloudService tencentCloudService;

    @Value("${TENCENT_CLOUD_ENV_ID:spring-3go98zd4f98e1fb9}")
    private String envId;

    @Value("${TENCENT_CLOUD_TOKEN:}")
    private String token;

    @Autowired
    private SinaRestClient sinaRestClient;

    @Autowired
    private TiantianFundRestClient tiantianFundRestClient;

    private static Gson gson = new Gson();

    /**
     * 基金列表页面
     */
    @GetMapping("/list")
    public String fundListPage(Model model) {
        return "user-fund";
    }

    /**
     * 获取用户基金列表API
     */
    @GetMapping("/api/list")
    @ResponseBody
    public Response getFundList(@RequestParam(required = false) String openId) {
        try {
            List<FundInfo> fundList = fundService.getUserFundList(openId);
            List<FundEntity> list = new ArrayList();
            if (!CollectionUtils.isEmpty(fundList)) {
                for (FundInfo fundInfo : fundList) {
                    FundEntity fundEntity = new FundEntity();
                    fundEntity.setApp(fundInfo.getOpenId());
                    fundEntity.setFundCode(fundInfo.getFundCode());
                    fundEntity.setBonds(fundInfo.getFundCount() + "");
                    fundEntity.setFundName(fundInfo.getFundName());
                    fundEntity.setCostPrise(fundInfo.getFundCost() + "");
                    fundEntity.setId(fundInfo.getId());
                    fundEntity.setOpenId(fundInfo.getOpenId());

                    list.add(fundEntity);
                }
            }

            Response response = new Response();
            response.setCode("00000000");
            response.setValue(list);
            System.out.println(response.toString());

            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取基金列表失败: " + e.getMessage());
            return Response.builder().build();
        }
    }

    /**
     * 获取所有基金列表API
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllFundList() {
        try {
            List<String> fundList = fundService.getAllFundListAsPO();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", fundList);
            response.put("total", fundList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取基金列表失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 根据基金代码获取基金信息API
     */
    @GetMapping("/api/detail/{fundCode}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFundDetail(@PathVariable String fundCode) {
        try {
            FundInfo fundInfo = fundService.getFundByCode(fundCode);

            Map<String, Object> response = new HashMap<>();
            if (fundInfo != null) {
                response.put("success", true);
                response.put("data", fundInfo);
            } else {
                response.put("success", false);
                response.put("message", "未找到基金代码: " + fundCode);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取基金详情失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 调用updateFundInfo云函数接口（同时保存本地数据库）
     *
     * @param request 包含基金信息的请求参数
     * @return 云函数执行结果
     */
    @PostMapping(value = "/api/updateFundInfo")
    @ResponseBody
    public Response updateFundInfo(@RequestBody Map<String, Object> request, HttpServletRequest servletRequest) {
        try {
            // 记录请求头信息，帮助排查前端请求是否正确到达后端
            log.info("updateFundInfo接口收到请求，请求路径: {}", servletRequest.getRequestURI());
            log.info("请求方法: {}", servletRequest.getMethod());
            log.info("Content-Type: {}", servletRequest.getContentType());
            log.info("请求参数: {}", request);

            Map<String, Object> payload = new HashMap<>();

            // 组装云函数需要的特定格式参数，添加详细日志记录每个参数的获取情况
            String id = (String) request.get("id");
            String code = (String) request.get("code");
            String name = (String) request.get("name");
            Object bonds = request.get("bonds");
            Object app = request.get("app");

            log.info("获取到的关键参数 - id: {}, code: {}, name: {}, bonds: {}, app: {}", id, code, name, bonds, app);

            payload.put("fundId", id);
            payload.put("fundCode", code);
            payload.put("fundName", name);
            payload.put("fundCount", bonds);
            payload.put("holdingProfit", request.get("holdingProfit")); // 修复：从request中获取holdingProfit字段而不是income
            payload.put("fundAmt", request.get("gsz"));
            payload.put("unitNetValue", request.get("dwjz"));
            payload.put("netValueDate", request.get("jzrq"));
            payload.put("openid", app);

            log.info("传递给云函数的参数: {}", payload);

            // 执行云函数调用
            log.info("准备调用云函数，envId: {}, functionName: updateFundInfo", envId);
            String result = tencentCloudService.callCloudFunction(envId, "updateFundInfo", token, payload);

            log.info("updateFundInfo云函数调用结果: {}", result);

            // 构建并记录响应信息
            Response response = Response.builder()
                .code("00000000")
                .msg("保存基金信息并调用updateFundInfo云函数成功")
                .value(result)
                .build();

            log.info("返回响应: {}", response);
            return response;

        } catch (Exception e) {
            log.error("调用updateFundInfo云函数失败，异常信息: {}", e.getMessage(), e);
            log.error("异常堆栈: ", e);

            Response errorResponse = Response.builder()
                .code("00000001")
                .msg("调用updateFundInfo云函数失败: " + e.getMessage())
                .value(null)
                .build();

            log.info("返回错误响应: {}", errorResponse);
            return errorResponse;
        }
    }

    /**
     * 保存基金信息并调用云函数进行更新
     *
     * @param fundRequest 基金请求信息
     * @param openId      用户openId（可选）
     * @return 保存结果和云函数调用结果
     */
    @PostMapping(value = "/api/saveAndUpdate")
    @ResponseBody
    public Response saveAndUpdateFund(@RequestBody com.buxuesong.account.apis.model.request.FundRequest fundRequest,
        @RequestParam(value = "openId", required = false) String openId) {
        try {
            log.info("保存基金信息并调用云函数，fundRequest: {}, openId: {}", fundRequest, openId);

            // 1. 先保存基金信息到本地数据库
            boolean saveResult = fundEntity.saveFund(fundRequest);

            if (!saveResult) {
                return Response.builder()
                    .code("00000001")
                    .msg("保存基金信息失败")
                    .value(null)
                    .build();
            }

            // 2. 保存成功后调用云函数进行更新
            Map<String, Object> payload = new HashMap<>();
            if (openId != null && !openId.isEmpty()) {
                payload.put("openId", openId);
            }
            // 添加基金信息到云函数参数中
            payload.put("fundCode", fundRequest.getCode());
            payload.put("fundName", fundRequest.getName());
            payload.put("fundId", fundRequest.getId());
            payload.put("fundCost", fundRequest.getCostPrise());
            payload.put("fundCount", fundRequest.getBonds());

            String cloudResult = tencentCloudService.callCloudFunction(envId, "updateFundInfo", token, payload);

            log.info("保存基金信息成功，云函数调用结果: {}", cloudResult);

            // 3. 返回综合结果
            Map<String, Object> result = new HashMap<>();
            result.put("saveSuccess", true);
            result.put("cloudFunctionResult", cloudResult);
            result.put("fundRequest", fundRequest);

            return Response.builder()
                .code("00000000")
                .msg("保存基金信息并调用云函数成功")
                .value(result)
                .build();

        } catch (Exception e) {
            log.error("保存基金信息并调用云函数失败", e);
            return Response.builder()
                .code("00000001")
                .msg("保存基金信息并调用云函数失败: " + e.getMessage())
                .value(null)
                .build();
        }
    }

    /**
     * 批量保存基金信息并调用云函数进行更新
     *
     * @param fundRequestList 基金请求信息列表
     * @param openId          用户openId（可选）
     * @return 批量保存结果和云函数调用结果
     */
    @PostMapping(value = "/api/batchSaveAndUpdate")
    @ResponseBody
    public Response batchSaveAndUpdateFunds(@RequestBody List<com.buxuesong.account.apis.model.request.FundRequest> fundRequestList,
        @RequestParam(value = "openId", required = false) String openId) {
        try {
            log.info("批量保存基金信息并调用云函数，数量: {}, openId: {}", fundRequestList.size(), openId);

            List<String> successList = new ArrayList<>();
            List<String> failList = new ArrayList<>();

            // 1. 批量保存基金信息
            for (com.buxuesong.account.apis.model.request.FundRequest fundRequest : fundRequestList) {
                try {
                    boolean saveResult = fundEntity.saveFund(fundRequest);
                    if (saveResult) {
                        successList.add(fundRequest.getCode());
                    } else {
                        failList.add(fundRequest.getCode());
                    }
                } catch (Exception e) {
                    log.error("保存基金信息失败: {}", fundRequest.getCode(), e);
                    failList.add(fundRequest.getCode());
                }
            }

            // 2. 如果有成功保存的基金，调用云函数进行更新
            String cloudResult = null;
            if (!successList.isEmpty()) {
                Map<String, Object> payload = new HashMap<>();
                if (openId != null && !openId.isEmpty()) {
                    payload.put("openId", openId);
                }
                payload.put("fundCodes", successList);

                cloudResult = tencentCloudService.callCloudFunction(envId, "updateFundInfo", token, payload);
                log.info("批量保存基金信息成功，云函数调用结果: {}", cloudResult);
            }

            // 3. 返回批量处理结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", fundRequestList.size());
            result.put("successCount", successList.size());
            result.put("failCount", failList.size());
            result.put("successList", successList);
            result.put("failList", failList);
            result.put("cloudFunctionResult", cloudResult);

            return Response.builder()
                .code("00000000")
                .msg("批量保存基金信息并调用云函数完成")
                .value(result)
                .build();

        } catch (Exception e) {
            log.error("批量保存基金信息并调用云函数失败", e);
            return Response.builder()
                .code("00000001")
                .msg("批量保存基金信息并调用云函数失败: " + e.getMessage())
                .value(null)
                .build();
        }
    }

    /**
     * 获取基金统计信息API
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFundStatistics(@RequestParam(required = false) String openId) {
        try {
            Map<String, Object> statistics = fundService.getFundStatistics(openId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}