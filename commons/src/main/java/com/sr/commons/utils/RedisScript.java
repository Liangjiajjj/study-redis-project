package com.sr.commons.utils;

public class RedisScript {

    // 扣库存脚本
    public static final String STOCK_SCRIPT =
            "if (redis.call('hexists', KEYS[1], KEYS[2]) == 1) then\n" +
                    "local stock = tonumber(redis.call('hget', KEYS[1], KEYS[2]));\n" +
                    "if (stock > 0) then\n" +
                    " redis.call('hincrby', KEYS[1], KEYS[2], -1);\n" +
                    "return stock;\n" +
                    "end;\n" +
                    "return 0;\n" +
                    "end;";

    // 抢红包脚本
    public static final String TAKE_LUCK_MONEY_SCRIPT =
            "if redis.call('hexists', KEYS[3], KEYS[4]) ~= 0 then\n" +
                    "  return nil\n" +
                    "else\n" +
                    "  local luck = redis.call('rpop', KEYS[1]);\n" +
                    "  if luck then\n" +
                    "    local x = cjson.decode(luck);\n" +
                    "    x['userId'] = KEYS[4];\n" +
                    "    local re = cjson.encode(x);\n" +
                    "    redis.call('hset', KEYS[3], KEYS[4], KEYS[4]);\n" +
                    "    redis.call('lpush', KEYS[2], re);\n" +
                    "    return re;\n" +
                    "  end\n" +
                    "end\n" +
                    "return nil";

}
