package com.sr.commons.constant;

import lombok.Getter;

@Getter
public enum RedisKeyConstant {

    verify_code("verify_code:", "验证码"),
    seckill_vouchers("seckill_vouchers:", "秒杀券的key"),
    lock_key("lockby:", "分布式锁的key"),
    following("following:", "关注集合Key"),
    followers("followers:", "粉丝集合key"),
    following_feeds("following_feeds:", "我关注的好友的FeedsKey"),
    diner_points("diner:points", "diner用户的积分Key"),
    diner_location("diner:location", "diner地理位置Key"),

    luck_money_list("luck_money_list:", "小红包队列"),
    luck_money_consumed_list("luck_money_consumed_list:", "小红包消费队列"),
    luck_money_consumed_map("luck_money_consumed_map", "红包用户列表"),
    ;

    private String key;
    private String desc;

    RedisKeyConstant(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

}