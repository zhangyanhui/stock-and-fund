package com.buxuesong.account.domain.service;

import com.alibaba.fastjson.JSONArray;
import com.buxuesong.account.apis.model.request.FundRequest;
import com.buxuesong.account.apis.model.response.SearchFundResult;
import com.buxuesong.account.domain.model.fund.FundEntity;
import com.buxuesong.account.infrastructure.adapter.rest.SinaRestClient;
import com.buxuesong.account.infrastructure.adapter.rest.TiantianFundRestClient;
import com.buxuesong.account.infrastructure.general.entity.FundInfo;
import com.buxuesong.account.infrastructure.general.utils.DateTimeUtils;
import com.buxuesong.account.infrastructure.general.utils.UserUtils;
import com.buxuesong.account.infrastructure.persistent.po.FundHisPO;
import com.buxuesong.account.infrastructure.persistent.po.FundJZPO;
import com.buxuesong.account.infrastructure.persistent.po.FundPO;
import com.buxuesong.account.infrastructure.persistent.repository.FundHisMapper;
import com.buxuesong.account.infrastructure.persistent.repository.FundJZMapper;
import com.buxuesong.account.infrastructure.persistent.repository.FundMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FundDetlService {

    @Autowired
    private SinaRestClient sinaRestClient;

    @Autowired
    private TiantianFundRestClient tiantianFundRestClient;

    private static Gson gson = new Gson();

    @Autowired
    private FundMapper fundMapper;

    @Autowired
    private FundHisMapper fundHisMapper;

    @Autowired
    private FundJZMapper fundJZMapper;

    @Autowired
    private CacheService cacheService;

    public List<FundEntity> getFundEntity(List<FundInfo> fundInfoList) {
        List<FundEntity> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fundInfoList)) {
            for (FundInfo fundInfo : fundInfoList) {
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
        return list;
    }

    public List<FundEntity> getFundDetails(List<String> codes) {
        List<FundEntity> funds = new ArrayList<>();
        List<String> codeList = new ArrayList<>();
        Map<String, String[]> codeMap = new HashMap<>();
        for (String str : codes) {
            String[] strArray;
            if (str.contains(",")) {
                strArray = str.split(",");
            } else {
                strArray = new String[] { str };
            }
            codeList.add(strArray[0]);
            codeMap.put(strArray[0], strArray);
        }

        for (String code : codeList) {
            try {
                String result = null;
                if (DateTimeUtils.isTradingTime()) {
                    result = tiantianFundRestClient.getFundInfo(code);
                } else {
                    result = cacheService.getFundInfoFromTiantianFund(code);
                }

                // 天天基金存在基金信息
                if (result != null && !result.equals("jsonpgz();")) {
                    String json = result.substring(8, result.length() - 2);
                    log.info("天天基金结果： {}", json);
                    if (!json.isEmpty()) {
                        FundEntity bean = gson.fromJson(json, FundEntity.class);
//                        FundEntity.loadFund(bean, codeMap);

                        BigDecimal now = new BigDecimal(bean.getGsz());
                        String costPriceStr = bean.getCostPrise();
                        if (costPriceStr != null && !costPriceStr.equals("--")) {
                            BigDecimal costPrice = new BigDecimal(costPriceStr);
                            BigDecimal incomePercent = now.subtract(costPrice).divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(new BigDecimal("100"));
                            bean.setIncomePercent(incomePercent.toString());
                            String bondsStr = bean.getBonds();
                            if (bondsStr != null && !bondsStr.equals("--")) {
                                BigDecimal bonds = new BigDecimal(bondsStr);
                                BigDecimal income = now.subtract(costPrice).multiply(bonds);
                                bean.setIncome(income.toString());
                            }
                        }
                        funds.add(bean);
                    }
                } else {
                    // 天天基金没有基金信息，从新浪获取
                    result = sinaRestClient.getFundInfo(code);
                    log.info("sina基金结果： {}", result);
                    FundEntity bean = FundEntity.loadFundFromSina(code, result);
//                    FundEntity.loadFund(bean, codeMap);
                    funds.add(bean);
                }
            } catch (Exception e) {
                log.error("获取基金信息异常 code: {}, error: {}", code, e.getMessage());
            }
        }
        return funds;
    }

    public boolean saveFund(FundRequest fundRequest) {
        String username = UserUtils.getUsername();
        try {
            String result = tiantianFundRestClient.getFundInfo(fundRequest.getCode());
            FundEntity bean = null;
            if (result != null && !result.equals("jsonpgz();")) {
                String json = result.substring(8, result.length() - 2);
                log.info("天天基金结果： {}", json);
                bean = gson.fromJson(json, FundEntity.class);
            } else {
                result = sinaRestClient.getFundInfo(fundRequest.getCode());
                log.info("sina基金结果： {}", result);
                bean = FundEntity.loadFundFromSina(fundRequest.getCode(), result);
            }
            fundRequest.setName(bean.getFundName());
        } catch (Exception e) {
            log.info("获取基金信息异常 {}", e.getMessage());
            return false;
        }
        FundPO fundPOFromTable = fundMapper.findFundByCode(fundRequest.getCode(), username);
        if (fundPOFromTable != null) {
            fundHisMapper.saveFromFund(fundRequest.getCode(), username);
            fundMapper.updateFund(FundPO.builder().name(fundRequest.getName()).app(fundRequest.getApp()).bonds(fundRequest.getBonds())
                .code(fundRequest.getCode())
                .costPrise(fundRequest.getCostPrise()).hide(fundRequest.isHide()).build(), username);
        } else {
            fundMapper.save(FundPO.builder().name(fundRequest.getName()).app(fundRequest.getApp()).bonds(fundRequest.getBonds())
                .code(fundRequest.getCode())
                .costPrise(fundRequest.getCostPrise()).hide(fundRequest.isHide()).build(), username);
        }
        return true;
    }

    public void deleteFund(FundRequest fundRequest) {
        String username = UserUtils.getUsername();
        fundHisMapper.saveFromFund(fundRequest.getCode(), username);
        fundMapper.deleteFund(FundPO.builder().app(fundRequest.getApp()).bonds(fundRequest.getBonds()).code(fundRequest.getCode())
            .costPrise(fundRequest.getCostPrise()).hide(fundRequest.isHide()).build(), username);
    }

    public List<String> getFundList(String app) {
        String username = UserUtils.getUsername();
        return getFundList(app, username);
    }

    public List<String> getFundList(String app, String username) {
        List<FundPO> fund = fundMapper.findAllFund(app, username);
        log.info("APP: {} ,数据库中的基金为：{}", app, fund);
        if (fund == null || fund.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (FundPO fundPO : fund) {
            String fundArr = fundPO.getCode() + "," + fundPO.getCostPrise() + "," + fundPO.getBonds() + ","
                + fundPO.getApp();
            list.add(fundArr);
        }
        return list;
    }

    public List<FundHisPO> getFundHisList(String app, String code, String beginDate, String endDate) {
        String username = UserUtils.getUsername();
        List<FundHisPO> fundHis = fundHisMapper.findAllFundHis(app, code, beginDate, endDate, username);
        FundPO fundPO = fundMapper.findFundByCode(code, username);
        FundHisPO fundHisPO = FundHisPO.builder()
            .app(fundPO.getApp())
            .code(fundPO.getCode())
            .createDate(DateTimeUtils.getLocalDateTime())
            .bonds(fundPO.getBonds())
            .bondsChange("0")
            .costPrise(fundPO.getCostPrise())
            .costPriseChange(new BigDecimal("0"))
            .hide(fundPO.isHide())
            .name(fundPO.getName())
            .build();
        fundHis.add(0, fundHisPO);
        log.info("APP: {} ,数据库中的基金历史为：{}", app, fundHis);
        FundHisPO next = null;
        for (int i = 0; i < fundHis.size(); i++) {
            FundHisPO current = fundHis.get(i);
            if (i + 1 < fundHis.size()) {
                next = fundHis.get(i + 1);
            }
            if (next != null) {
                next.setBondsChange((new BigDecimal(current.getBonds()).subtract(new BigDecimal(next.getBonds()))).toString());
                next.setCostPriseChange(current.getCostPrise().subtract(next.getCostPrise()));
            }
        }
        return fundHis;
    }

    public List<SearchFundResult> searchFundByName(String name) {
        List<SearchFundResult> result = new ArrayList<>();
        String funds = cacheService.searchAllFundsFromEastMoney();
        funds = funds.replace("var r = ", "").replace(";", "");
        JSONArray jsonArray = JSONArray.parseArray(funds);

        // 遍历 JSONArray
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray innerArray = jsonArray.getJSONArray(i);
            // 判断内部数组的第三个元素是否包含搜索名称，或者第一个元素是否包括基金编码
            if (innerArray.getString(2).contains(name) || innerArray.getString(0).contains(name)) {
                SearchFundResult searchFundResult = new SearchFundResult();
                searchFundResult.setFundCode(innerArray.getString(0));
                searchFundResult.setFundName(innerArray.getString(2));
                result.add(searchFundResult);
            }
        }
        log.info("通过基金名称: {} 搜索到的结果为：{}", name, result);
        return result;
    }

    public void getRecentDateUpper(List<FundJZPO> fundJZPOs, FundEntity bean) {
        if (fundJZPOs == null || fundJZPOs.isEmpty()) {
            return;
        }

        // 获取最新日期
        LocalDate latestDate = LocalDate.parse(fundJZPOs.get(fundJZPOs.size() - 1).getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);

        // 一年前
        LocalDate oneYearAgoDate = latestDate.minusYears(1);
        FundJZPO oneYearAgoDateDayHistory = null;
        for (int i = fundJZPOs.size() - 1; i >= 0; i--) {
            FundJZPO current = fundJZPOs.get(i);
            LocalDate currentDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (currentDate.compareTo(oneYearAgoDate) <= 0) {
                oneYearAgoDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
                oneYearAgoDateDayHistory = current;
                break;
            }
        }

        // 一季度前
        LocalDate oneSeasonAgoDate = latestDate.minusMonths(3);
        FundJZPO oneSeasonAgoDateDayHistory = null;
        for (int i = fundJZPOs.size() - 1; i >= 0; i--) {
            FundJZPO current = fundJZPOs.get(i);
            LocalDate currentDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (currentDate.compareTo(oneSeasonAgoDate) <= 0) {
                oneSeasonAgoDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
                oneSeasonAgoDateDayHistory = current;
                break;
            }
        }

        // 一月前
        LocalDate oneMonthAgoDate = latestDate.minusMonths(1);
        FundJZPO oneMonthAgoDateDayHistory = null;
        for (int i = fundJZPOs.size() - 1; i >= 0; i--) {
            FundJZPO current = fundJZPOs.get(i);
            LocalDate currentDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (currentDate.compareTo(oneMonthAgoDate) <= 0) {
                oneMonthAgoDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
                oneMonthAgoDateDayHistory = current;
                break;
            }
        }

        // 一周前
        LocalDate oneWeekAgoDate = latestDate.minusWeeks(1);
        FundJZPO oneWeekAgoDateDayHistory = null;
        for (int i = fundJZPOs.size() - 1; i >= 0; i--) {
            FundJZPO current = fundJZPOs.get(i);
            LocalDate currentDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
            if (currentDate.compareTo(oneWeekAgoDate) <= 0) {
                oneWeekAgoDate = LocalDate.parse(current.getFSRQ(), DateTimeFormatter.ISO_LOCAL_DATE);
                oneWeekAgoDateDayHistory = current;
                break;
            }
        }

        // 设置历史数据
        if (oneYearAgoDateDayHistory != null) {
            bean.setOneYearAgoUpper(oneYearAgoDateDayHistory.getDWJZ());
        }
        if (oneSeasonAgoDateDayHistory != null) {
            bean.setOneSeasonAgoUpper(oneSeasonAgoDateDayHistory.getDWJZ());
        }
        if (oneMonthAgoDateDayHistory != null) {
            bean.setOneMonthAgoUpper(oneMonthAgoDateDayHistory.getDWJZ());
        }
        if (oneWeekAgoDateDayHistory != null) {
            bean.setOneWeekAgoUpper(oneWeekAgoDateDayHistory.getDWJZ());
        }
    }
}