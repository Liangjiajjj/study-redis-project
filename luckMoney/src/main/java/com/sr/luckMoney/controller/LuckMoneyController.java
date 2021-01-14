package com.sr.luckMoney.controller;

import com.sr.luckMoney.service.LuckMoneyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class LuckMoneyController {
    @Resource
    private LuckMoneyService service;

    @PostMapping("addLuckMoney")
    public void addLuckMoney(int luckMoneyId, long total, int count, long max, long min) {
        service.addLuckMoney(luckMoneyId, total, count, max, min);
    }

    @PostMapping("takeLuckMoney")
    public void takeLuckMoney(int luckMoneyId, int useId) {
        service.takeLuckMoney(luckMoneyId, useId);
    }

    @PostMapping("test")
    public void test() {
        service.takeLuckMoney(1, 2);
    }
}
