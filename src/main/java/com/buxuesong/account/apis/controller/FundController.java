package com.buxuesong.account.apis.controller;

import com.buxuesong.account.apis.model.request.FundRequest;
import com.buxuesong.account.apis.model.response.Response;
import com.buxuesong.account.domain.model.fund.FundEntity;
import com.buxuesong.account.apis.model.response.StockAndFundBean;
import com.buxuesong.account.domain.model.stock.StockEntity;
import com.buxuesong.account.infrastructure.general.util.HttpClientUtil;
import com.buxuesong.account.infrastructure.general.service.TencentCloudService;
import com.buxuesong.account.infrastructure.persistent.po.FundHisPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.buxuesong.account.infrastructure.general.utils.UserUtils;

@Slf4j
@RestController
public class FundController {

    @Autowired
    private FundEntity fundEntity;

    @Autowired
    private StockEntity stockEntity;

    @Autowired
    private TencentCloudService tencentCloudService;

    @Value("${TENCENT_CLOUD_ENV_ID:spring-3go98zd4f98e1fb9}")
    private String envId;

    @Value("${TENCENT_CLOUD_TOKEN:}")
    private String token;

    /**
     * 获取基金信息列表接口
     *
     * @return
     */
    @GetMapping(value = "/fund")
    public Response getFundList(@RequestParam(value = "app", required = false) String app,
        @RequestParam(value = "code", required = false) String code) throws Exception {
        List<String> fundList = fundEntity.getFundList(app);
        if (code != null && !"".equals(code)) {
            fundList = fundList.stream().filter(s -> s.contains(code)).collect(Collectors.toList());
        }
        return Response.builder().code("00000000").value(fundEntity.getFundDetails(fundList)).build();
    }

    @GetMapping(value = "/fundHis")
    public Response getFundHisList(@RequestParam(value = "app", required = false) String app,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "beginDate", required = false) String beginDate,
        @RequestParam(value = "endDate", required = false) String endDate) throws Exception {
        List<FundHisPO> fundHisList = fundEntity.getFundHisList(app, code, beginDate, endDate);
        return Response.builder().code("00000000").value(fundHisList).build();
    }

    @GetMapping(value = "/fund/search")
    public Response searchFundByName(@RequestParam(value = "name", required = false) String name) throws Exception {
        return Response.builder().code("00000000").value(fundEntity.searchFundByName(name)).build();
    }

    /**
     * 保存/修改基金接口
     *
     * @return
     */
    @PostMapping(value = "/saveFund")
    public Response saveFund(@RequestBody FundRequest fundRequest) throws Exception {
        log.info("Save fund request: {}", fundRequest);
        if (fundEntity.saveFund(fundRequest)) {
            // 保存成功后调用云函数进行更新
            String openId = UserUtils.getUsername(); // 获取当前用户的openId
            if (StringUtils.isNotEmpty(openId)) {
                log.info("调用云函数更新基金信息，openId: {}", openId);
                String response = tencentCloudService.callUpdateFundInfo(envId, token, openId);
                log.info("云函数调用结果: {}", response);
            }
            return Response.builder().value(true).code("00000000").build();
        }
        return Response.builder().value(true).code("00000001").build();
    }

    /**
     * 删除基金接口
     *
     * @return
     */
    @PostMapping(value = "/deleteFund")
    public Response deleteFund(@RequestBody FundRequest fundRequest) throws Exception {
        log.info("Delete fund request: {}", fundRequest);
        fundEntity.deleteFund(fundRequest);
        return Response.builder().value(true).code("00000000").build();
    }

    /**
     * 获取基金信息列表接口
     *
     * @return
     */
    @GetMapping(value = "/stockAndFund")
    public Response getStockAndFundList(@RequestParam(value = "app", required = false) String app)
        throws Exception {
        List<String> fundListFrom = fundEntity.getFundList(app);
        List<String> stcokListFrom = stockEntity.getStockList(app);
        List<FundEntity> funds = fundEntity.getFundDetails(fundListFrom);
        List<StockEntity> stocks = stockEntity.getStockDetails(stcokListFrom);
        List<StockAndFundBean> stockAndFundsFromFunds = funds.stream()
            .map(s -> StockAndFundBean.builder().type("FUND").code(s.getFundCode())
                .name(s.getFundName()).costPrise(s.getCostPrise()).bonds(s.getBonds())
                .app(s.getOpenId()).incomePercent(s.getIncomePercent()).income(s.getIncome())
                // 基金部分内容
                .jzrq(s.getJzrq()).dwjz(s.getDwjz()).gsz(s.getGsz())
                .gszzl(s.getGszzl()).gztime(s.getGztime())
                .currentDayJingzhi(s.getCurrentDayJingzhi())
                .previousDayJingzhi(s.getPreviousDayJingzhi())
                .oneYearAgoUpper(s.getOneYearAgoUpper())
                .oneSeasonAgoUpper(s.getOneSeasonAgoUpper())
                .oneMonthAgoUpper(s.getOneMonthAgoUpper())
                .oneWeekAgoUpper(s.getOneWeekAgoUpper())
                .build())
            .collect(Collectors.toList());
        List<StockAndFundBean> stockAndFundsFromStocks = stocks.stream()
            .map(s -> StockAndFundBean.builder().type("STOCK").code(s.getCode()).name(s.getName())
                .costPrise(s.getCostPrise()).bonds(s.getBonds()).app(s.getApp())
                .incomePercent(s.getIncomePercent()).income(s.getIncome())
                // 股票部分内容
                .now(s.getNow()).change(s.getChange()).changePercent(s.getChangePercent())
                .time(s.getTime()).max(s.getMax()).min(s.getMin())
                .buyOrSellStockRequestList(s.getBuyOrSellStockRequestList())
                .day50Min(s.getDay50Min()).day50Max(s.getDay50Max())
                .day20Min(s.getDay20Min()).day20Max(s.getDay20Max())
                .day10Min(s.getDay10Min()).day10Max(s.getDay10Max())
                .oneYearAgoUpper(s.getOneYearAgoUpper())
                .oneSeasonAgoUpper(s.getOneSeasonAgoUpper())
                .oneMonthAgoUpper(s.getOneMonthAgoUpper())
                .oneWeekAgoUpper(s.getOneWeekAgoUpper())
                .build())
            .collect(Collectors.toList());
        stockAndFundsFromStocks.addAll(stockAndFundsFromFunds);
        return Response.builder().code("00000000").value(stockAndFundsFromStocks).build();
    }

    public static void main(String[] args) {
// 使用示例
        String result = HttpClientUtil.callTencentCloudFunction("spring-3go98zd4f98e1fb9", "getAllUserFunds",
            "eyJhbGciOiJSUzI1NiIsImtpZCI6IjlkMWRjMzFlLWI0ZDAtNDQ4Yi1hNzZmLWIwY2M2M2Q4MTQ5OCJ9.eyJhdWQiOiJzcHJpbmctM2dvOTh6ZDRmOThlMWZiOSIsImV4cCI6MjUzNDAyMzAwNzk5LCJpYXQiOjE3NTgyNDgwMTEsImF0X2hhc2giOiJQTnhnMXBULUVmQ083VkpVQU5namh3IiwicHJvamVjdF9pZCI6InNwcmluZy0zZ285OHpkNGY5OGUxZmI5IiwibWV0YSI6eyJwbGF0Zm9ybSI6IkFwaUtleSJ9LCJhZG1pbmlzdHJhdG9yX2lkIjoiMTg2OTY0ODE0MTQyOTU0NzAxMCIsInVzZXJfdHlwZSI6IiJ9.i3hPKdGKc2rwjzr97q3Z3pvqlYZDmhdE3vX-VWtbFEaK1o928AOfYrhMkibb1XsPTr0MjXHM2cMJgJlNdfFmj8pARk2yNfGTpmsuWQ2KQl4EnrPaIusI1H7L3tMrPf8HZZOEtkyzdu1YQyB20StqQrD59mWujgDEKIavV14SS-11MYkKoiCAAfXJlJyXm9Rb3V7J4iotC7TKCY2xcgCbpKj4dBjDS75N_u07ekHDx_aDl9GkJegnIJG-R-fPR2VxZvxaW5dBmbEdw5ihDbY0lwXETuXDJnFLvNhhVZxeS9JYvWzLWEaXdngu8ybQ0M2SIyZ75ZBTgAps6t37kMZ4UA");
        System.out.println(result);

    }
}
