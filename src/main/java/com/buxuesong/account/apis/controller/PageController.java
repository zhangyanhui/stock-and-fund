package com.buxuesong.account.apis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器 用于处理页面跳转请求
 */
@Controller
public class PageController {

    /**
     * 跳转到股票基金汇总页面
     *
     * @return 股票基金汇总页面
     */
    @GetMapping(value = "/stockAndFund")
    public String stockAndFund() {
        return "stockAndFund";
    }

    /**
     * 跳转到基金列表页面
     *
     * @return 基金列表页面
     */
    @GetMapping(value = "/fund-list")
    public String fundList() {
        return "fund-list";
    }

    /**
     * 跳转到股票页面
     *
     * @return 股票页面
     */
    @GetMapping(value = "/stock")
    public String stock() {
        return "stock";
    }

    /**
     * 跳转到基金页面
     *
     * @return 基金页面
     */
    @GetMapping(value = "/fund")
    public String fund() {
        return "fund";
    }

    /**
     * 跳转到存款页面
     *
     * @return 存款页面
     */
    @GetMapping(value = "/deposit")
    public String deposit() {
        return "deposit";
    }

    /**
     * 跳转到参数页面
     *
     * @return 参数页面
     */
    @GetMapping(value = "/param")
    public String param() {
        return "param";
    }
}
