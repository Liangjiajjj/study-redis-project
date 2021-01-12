package com.sr.luckMoney.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.sr.commons.constant.RedisKeyConstant;
import com.sr.commons.model.pojo.LuckMoneyInfo;
import com.sr.commons.utils.AssertUtil;
import com.sr.commons.utils.RedisScript;
import com.sr.luckMoney.util.LuckMoneyAlgorithm;
import jodd.util.CollectionUtil;
import org.redisson.api.RQueue;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LuckMoneyService {
    @Resource
    private RedissonClient redissonClient;

    // 添加红包
    public void addLuckMoney(int luckMoneyId, long total, int count, long max, long min) {
        RQueue<Object> queue = redissonClient.getQueue(RedisKeyConstant.luck_money_list.getKey() + ":" + luckMoneyId);
        AssertUtil.isTrue(!CollectionUtils.isEmpty(queue), "红包已存在");
        long[] generate = LuckMoneyAlgorithm.generate(total, count, max, min);
        for (long money : generate) {
            LuckMoneyInfo info = new LuckMoneyInfo();
            info.setId(luckMoneyId);
            info.setMoney(money);
            info.setUserId(0);
            info.setCreateTime(new Date());
            // 默认两天后结束
            Date endDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));
            info.setEndTime(endDate);
            queue.add(info);
        }
    }

    // 抢红包
    public void takeLuckMoney(int luckMoneyId, int userId) {
        // 未消费列表
        String luck_money_list_key = RedisKeyConstant.luck_money_list.getKey() + ":" + luckMoneyId;
        // 已消费列表
        String luck_money_consumed_list_key = RedisKeyConstant.luck_money_consumed_list.getKey() + ":" + luckMoneyId;
        // 消费者map
        String luck_money_consumed_map_key = RedisKeyConstant.luck_money_consumed_map.getKey() + ":" + luckMoneyId;

        List<Object> keys = new ArrayList<>();
        keys.add(luck_money_list_key);
        keys.add(luck_money_consumed_list_key);
        keys.add(luck_money_consumed_map_key);
        keys.add(userId);
        // 调用脚本
        LuckMoneyInfo info = redissonClient.getScript().eval(RScript.Mode.READ_ONLY, RedisScript.TAKE_LUCK_MONEY_SCRIPT, RScript.ReturnType.VALUE, keys);
        AssertUtil.isTrue(info == null, "未抢到红包");

        // 判断条件
        Date now = new Date();
        AssertUtil.isTrue(now.after(info.getEndTime()), "该抢购已结束");
        // 入库 ...
        // 发奖 ...
    }


}
