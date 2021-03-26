package com.sr.commons.expand;

import org.redisson.api.RFuture;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 可以继承下queue类实现所有的接口
 * dome用就不用了
 */
public class RLocalCachedQueue<V> {
    private ConcurrentLinkedQueue<V> cQueue = new ConcurrentLinkedQueue<V>();
    private RQueue<V> rQueue;
    private RTopic rTopic;

    public RLocalCachedQueue(Class clazz, String name, RedissonClient client) {
        super();
        this.rQueue = client.getQueue(name);
        this.rTopic = client.getTopic(name);
        this.rTopic.addListener(clazz, (channel, msg) -> {
            if (msg == null) {
                return;
            }
            V v = (V) msg;
            cQueue.add(v);
        });
        this.cQueue.addAll(rQueue.readAll());
    }

    public RFuture<Boolean> add(V v) {
        RFuture<Boolean> future = rQueue.offerAsync(v);
        rTopic.publish(v);
        return future;
    }

    public ConcurrentLinkedQueue<V> getQueue() {
        return cQueue;
    }
}
