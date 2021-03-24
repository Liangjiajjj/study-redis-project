1.先从未消费队列拿出小红包
2.通过useId判断map是否拿过该队列红包
3.push另外一个已经消费队列，最后把id加入map

参数：
1.红包列表名
2.已经消费列表名
3.去重map名
4.用户名

--查询已经消费者列表是否已经存在
if redis.call('hexists',KEYS[3],KEYS[4]) ~= 0 then
    return nil
else
    local luck = redis.call('rpop',KEYS[1]);
    if luck then
        -- 把红包json序列化
        local x = cjson.decode(luck);
        -- 赋值json的userId为参数4
        x['userId'] = KEYS[4];
        local re = cjson.encode(x);
        --插入到消费者map
        redis.call('hset', KEYS[3], KEYS[4], KEYS[4]);
        --加入已经消费队列
        redis.call('lpush',KEYS[2],re);
        --返回已经消费的红包
        return re;
    end
end
return nil

