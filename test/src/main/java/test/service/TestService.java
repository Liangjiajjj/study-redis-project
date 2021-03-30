package test.service;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class TestService {

    @Resource
    private RedissonClient client;

    private LocalCachedMapOptions update_options = LocalCachedMapOptions.defaults().syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE);

    /**
     * 使用锁 + 缓存map
     */
    private RLocalCachedMap<Integer, Integer> map;

    @PostConstruct
    public void init() {
        map = client.getLocalCachedMap("test", update_options);
    }

    public RLocalCachedMap<Integer, Integer> getMap() {
        return map;
    }
}
