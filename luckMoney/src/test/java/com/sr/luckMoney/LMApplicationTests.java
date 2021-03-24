package com.sr.luckMoney;

import org.junit.jupiter.api.Test;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class LMApplicationTests {
    @Resource
    private RedissonClient redisson;

    @Test
    public void testMap() throws InterruptedException {
        RLocalCachedMap<Object, Object> map = redisson.getLocalCachedMap("key",
                LocalCachedMapOptions.defaults().cacheSize(1).syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE));
        map.put(1,2);
        Thread.sleep(50000);
    }
}
