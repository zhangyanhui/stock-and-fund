package com.buxuesong.account.infrastructure.general.controller;

import com.buxuesong.account.infrastructure.general.entity.FundInfo;
import com.buxuesong.account.infrastructure.general.service.FundService;
import com.buxuesong.account.infrastructure.persistent.po.FundPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金列表控制器
 */
@Controller
@RequestMapping("/fund")
public class FundListController {

    @Autowired
    private FundService fundService;

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
    public ResponseEntity<Map<String, Object>> getFundList(@RequestParam(required = false) String openId) {
        try {
            List<FundPO> fundList = fundService.getUserFundListAsPO(openId);
            Map<String, Object> statistics = fundService.getFundStatistics(openId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", fundList);
            response.put("total", fundList.size());
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取基金列表失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取所有基金列表API
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllFundList() {
        try {
            List<FundPO> fundList = fundService.getAllFundListAsPO();

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