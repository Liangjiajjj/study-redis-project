package com.sr.scekill.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟redis锁的脚本
 */
public class JavaRedisDome {

    private Map<String, Map<Integer, AtomicInteger>> LockMap = new ConcurrentHashMap<>();

    public boolean _lock(String lockName, int threadId) {
        Map<Integer, AtomicInteger> map = LockMap.get(lockName);
        if (map == null) {
            Map<Integer, AtomicInteger> threadMap = new ConcurrentHashMap<>();
            threadMap.put(threadId, new AtomicInteger(1));
            LockMap.put(lockName, threadMap);
            // 设置过期时间 ...
            return true;
        }
        if (map != null) {
            AtomicInteger count = map.get(threadId);
            /* 证明不是当前线程持有的锁 */
            if (count == null) {
                return false;
            }
            count.addAndGet(1);
            // 设置过期时间 ...
            return true;
        }
        return false;
    }

    public boolean _unlock(String lockName, int threadId) {
        Map<Integer, AtomicInteger> map = LockMap.get(lockName);
        // 完全没有锁
        if (map == null) {
            return false;
        }
        AtomicInteger atomic_count = map.get(threadId);
        /* 证明不是当前线程持有的锁 */
        if (atomic_count == null) {
            return false;
        }
        int count = atomic_count.addAndGet(-1);
        if (count == 0) {
            LockMap.remove(lockName);
            return true;
        }
        return false;
    }
}