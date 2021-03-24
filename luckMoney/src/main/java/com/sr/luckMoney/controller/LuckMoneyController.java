package com.sr.luckMoney.controller;

import com.sr.commons.constant.RedisKeyConstant;
import com.sr.commons.model.pojo.LuckMoneyInfo;
import com.sr.commons.utils.AssertUtil;
import com.sr.luckMoney.service.LuckMoneyService;
import com.sr.luckMoney.util.LuckMoneyAlgorithm;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
public class LuckMoneyController {

    @Resource
    private RedissonClient redissonClient;

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

    @GetMapping("test")
    public void test() {
        LocalCachedMapOptions<Object, Object> options = LocalCachedMapOptions.defaults().cacheSize(1).syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE);
        RMap<Object, Object> map = redissonClient.getLocalCachedMap("key1", options);
        // map.put(1, 22);
    }
}
