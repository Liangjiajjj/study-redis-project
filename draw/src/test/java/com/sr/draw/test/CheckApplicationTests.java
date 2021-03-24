package com.sr.draw.test;

import draw.DrawApplication;
import org.junit.jupiter.api.Test;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = DrawApplication.class)
public class CheckApplicationTests {

    @Resource
    private RedissonClient client;

    @Test
    public void test() {
        for (int i = 0; i < 1000; i++) {
            RMap<Integer, Integer> map = client.getMap("test");
            RFuture<Integer> future = map.addAndGetAsync(1, 1);
            future.onComplete((value1, throwable) -> {
                System.out.println("value1 ==============" + value1);
                Object value2 = client.getMap("test").getOrDefault(1, 0);
                System.out.println("value2 ==============" + value2);
            });
        }
    }

    @Test
    public void test2() {
        for (int i = 0; i < 10000; i++) {
            client.getMap("test").getOrDefault(1, 0);
        }
    }
}
