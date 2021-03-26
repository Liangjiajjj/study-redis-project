package com.sr.draw.test;

import com.sr.commons.expand.RLocalCachedQueue;
import com.sr.commons.utils.RedisScript;
import draw.DrawApplication;
import draw.model.Result;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = DrawApplication.class)
public class CheckApplicationTests {

    /**
     * LocalCachedMapOptions options = LocalCachedMapOptions.defaults()
     * // 用于淘汰清除本地缓存内的元素
     * // 共有以下几种选择:
     * // LFU - 统计元素的使用频率，淘汰用得最少（最不常用）的。
     * // LRU - 按元素使用时间排序比较，淘汰最早（最久远）的。
     * // SOFT - 元素用Java的WeakReference来保存，缓存元素通过GC过程清除。
     * // WEAK - 元素用Java的SoftReference来保存, 缓存元素通过GC过程清除。
     * // NONE - 永不淘汰清除缓存元素。
     * .evictionPolicy(EvictionPolicy.NONE)
     * // 如果缓存容量值为0表示不限制本地缓存容量大小
     * .cacheSize(1000)
     * // 以下选项适用于断线原因造成了未收到本地缓存更新消息的情况。
     * // 断线重连的策略有以下几种：
     * // CLEAR - 如果断线一段时间以后则在重新建立连接以后清空本地缓存
     * // LOAD - 在服务端保存一份10分钟的作废日志
     * //        如果10分钟内重新建立连接，则按照作废日志内的记录清空本地缓存的元素
     * //        如果断线时间超过了这个时间，则将清空本地缓存中所有的内容
     * // NONE - 默认值。断线重连时不做处理。
     * .reconnectionStrategy(ReconnectionStrategy.NONE)
     * // 以下选项适用于不同本地缓存之间相互保持同步的情况
     * // 缓存同步策略有以下几种：
     * // INVALIDATE - 默认值。当本地缓存映射的某条元素发生变动时，同时驱逐所有相同本地缓存映射内的该元素
     * // UPDATE - 当本地缓存映射的某条元素发生变动时，同时更新所有相同本地缓存映射内的该元素
     * // NONE - 不做任何同步处理
     * .syncStrategy(SyncStrategy.INVALIDATE)
     * // 每个Map本地缓存里元素的有效时间，默认毫秒为单位
     * .timeToLive(10000)
     * // 或者
     * .timeToLive(10, TimeUnit.SECONDS)
     * // 每个Map本地缓存里元素的最长闲置时间，默认毫秒为单位
     * .maxIdle(10000)
     * // 或者
     * .maxIdle(10, TimeUnit.SECONDS);
     */
    private LocalCachedMapOptions options = LocalCachedMapOptions.defaults();

    private LocalCachedMapOptions update_options = LocalCachedMapOptions.defaults().syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE);


    @Resource
    private RedissonClient client;


    /**
     * Redisson错误示范1，在异步RFuture里面用同步方法
     * 导致同时阻塞两个线程
     */
    @Test
    public void test1() {
        for (int i = 0; i < 1000; i++) {
            RMap<Integer, Integer> map = client.getMap("test");
            RFuture<Integer> future = map.addAndGetAsync(1, 1);
            future.onComplete((value1, throwable) -> {
                System.out.println("value1 ==============" + value1);
                Object value2 = client.getMap("test").addAndGet(1, 1);
                System.out.println("value2 ==============" + value2);
            });
        }
    }


    /**
     * Redisson错误示范2
     * 在同一业务中，多次使用同步查询
     */
    @Test
    public void test2() {
        for (int i = 0; i < 10000; i++) {
            client.getMap("test").getOrDefault(1, 0);
        }
    }


    /**
     * Redisson解决方案1
     * 问题：在同一业务中，多次使用同步查询
     * 方案：如果这个请求是用于了锁，那只需要查询一次，如果有修改最后在加回去
     */
    @Test
    public void solve1() {
        RLock lock = client.getLock("testLock");
        boolean isLock = lock.tryLock();
        if (isLock) {
            try {
                RMap<Integer, Integer> map = client.getMap("test");
                Integer num = map.getOrDefault(1, 0);
                Result result = new Result(num);
                for (int i = 0; i < 10000; i++) {
                    result.addValue(1);
                }
                map.put(1, result.getValue());
            } finally {
                lock.unlock();
            }
        }
    }


    /**
     * Redisson解决方案2
     * 问题：在同一业务中，多次使用同步查询
     * 方案：如果改数据不是频繁修改，建议使用缓存（RLocalCachedMap）
     */
    @Test
    public void solve2() {
        RLocalCachedMap<Integer, Integer> map = client.getLocalCachedMap("test", options);
        for (int i = 0; i < 1000; i++) {
            Integer value = map.getOrDefault(1, 0);
            System.out.println("value :" + value);
        }
    }


    /**
     * RLocalCachedMap
     */
    @Test
    public void dome1() {
        RLocalCachedMap<Integer, Integer> map = client.getLocalCachedMap("test", options);
        map.put(1, 1);
    }


    /**
     * RLocalCachedQueue
     */
    @Test
    public void dome2() {
        RLocalCachedQueue<Integer> queue = new RLocalCachedQueue(Integer.class, "queue", client);
        queue.add(1);
        RFuture<Boolean> future = queue.add(2);
        future.onComplete((aBoolean, throwable) -> {
            for (Integer value : queue.getQueue()) {
                System.out.println("value ==========================  : " + value);
            }
        });
    }


    /**
     * Redisson优化写的方案 1
     * 方案：使用redis的管道功能
     */
    @Test
    public void write_solve1() {
        RBatch batch = client.createBatch();
        for (int i = 0; i < 100; i++) {
            batch.getMap("test").addAndGetAsync(1, 1);
        }
        batch.executeAsync();
    }

    /**
     * Redisson优化写的方案 2
     * 方案：使用Redis的脚本
     */
    @Test
    public void write_solve2() {
        List<Object> keys = new ArrayList<>();
        keys.add("\"stock\"");
        keys.add("\"amount\"");
        client.getScript().evalAsync(RScript.Mode.READ_ONLY, RedisScript.STOCK_SCRIPT, RScript.ReturnType.INTEGER, keys);
    }

    /**
     * 使用锁 + 缓存map
     */
    @Test
    public void redisLockTest() {
        RLocalCachedMap<Integer, Integer> map = client.getLocalCachedMap("test", update_options);
        RLock lock = client.getLock("testLock");
        boolean isLock = lock.tryLock();
        if (isLock) {
            try {
                for (int i = 0; i < 1000; i++) {
                    Integer value = map.getOrDefault(1, 0);
                    if (value > 10) {
                        // dosome...
                    }
                }
                // 同步处理必要数据
                // 中间操作异步执行了~
            } finally {
                lock.unlock();
            }
        }
    }


    /**
     * 使用并发修改
     */
    @Test
    public void redisAddAndGetTest() {
        // RLocalCachedMap<Integer, Integer> map = client.getLocalCachedMap("test", update_options);
        RMap<Integer, Integer> map = client.getMap("test");
        for (int i = 0; i < 1000; i++) {
            // 先加后判断，保持原子性操作
            Integer value = map.addAndGet(1, 1);
            if (value > 10) {
                // dosome...
            }
            // 退回操作
            map.addAndGetAsync(1, -1);
            // 中间操作异步执行了~
        }
        // 在真正获取这个物品的时候才加回去
    }
}
