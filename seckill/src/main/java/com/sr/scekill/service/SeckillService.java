package com.sr.scekill.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.sr.commons.constant.RedisKeyConstant;
import com.sr.commons.model.domain.ResultInfo;
import com.sr.commons.model.pojo.SeckillVouchers;
import com.sr.commons.model.pojo.VoucherOrders;
import com.sr.commons.utils.AssertUtil;
import com.sr.commons.utils.ResultInfoUtil;
import com.sr.scekill.mapper.SeckillVouchersMapper;
import com.sr.scekill.mapper.VoucherOrdersMapper;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillService {

    // 扣库存脚本
    private static final String STOCK_SCRIPT =
            "if (redis.call('hexists', KEYS[1], KEYS[2]) == 1) then\n" +
                    "local stock = tonumber(redis.call('hget', KEYS[1], KEYS[2]));\n" +
                    "if (stock > 0) then\n" +
                    " redis.call('hincrby', KEYS[1], KEYS[2], -1);\n" +
                    "return stock;\n" +
                    "end;\n" +
                    "return 0;\n" +
                    "end;";

    @Resource
    private SeckillVouchersMapper seckillVouchersMapper;
    @Resource
    private VoucherOrdersMapper voucherOrdersMapper;
    @Resource
    private RedissonClient redissonClient;

    // 添加代金券
    @Transactional(rollbackFor = Exception.class) // 事务是否回退
    public void addSeckillVouchers(SeckillVouchers seckillVouchers) {
        AssertUtil.isNotNull(seckillVouchers, "代金券实体为空");
        AssertUtil.isTrue(seckillVouchers.getFkVoucherId() == 0, "请输入需要抢购的代金券");
        AssertUtil.isTrue(seckillVouchers.getAmount() <= 0, "请输入抢购总数量");
        Date now = new Date();
        AssertUtil.isNotNull(seckillVouchers.getStartTime(), "请输入开始时间");
        AssertUtil.isNotNull(seckillVouchers.getEndTime(), "请输入结束时间");
        // AssertUtil.isTrue(now.after(seckillVouchers.getStartTime()), "开始时间不能早于当前时间");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "结束时间不能早于当前时间");
        AssertUtil.isTrue(seckillVouchers.getStartTime().after(seckillVouchers.getEndTime()), "开始时间不能早于结束时间");

        // 是否已经有秒杀活动
        String redisKey = RedisKeyConstant.seckill_vouchers.getKey() + seckillVouchers.getFkVoucherId();
        RMap<Object, Object> seckillVoucherMaps = redissonClient.getMap(redisKey);
        AssertUtil.isTrue(!seckillVoucherMaps.isEmpty() && (int) seckillVoucherMaps.get("amount") > 0, "该卷已经拥有了活动");
        // 同步到redis上面
        seckillVouchers.setIsValid(1);
        seckillVouchers.setCreateDate(new Date());
        seckillVouchers.setUpdateDate(new Date());
        // 保存到redis上
        seckillVoucherMaps.putAll(BeanUtil.beanToMap(seckillVouchers));
    }

    // 客户端抢代金券（基于Redis）
    @Transactional(rollbackFor = Exception.class) // 事务是否回退
    public ResultInfo doSeckill(Integer voucherId, int userId, String path) {
        // 基本参数校验
        AssertUtil.isTrue(voucherId == null || voucherId < 0, "请选择需要抢购的代金券");
        // 判断此代金券是否加入抢购
        String redisKey = RedisKeyConstant.seckill_vouchers.getKey() + voucherId;
        RMap<Object, Object> seckillVoucherMaps = redissonClient.getMap(redisKey);
        SeckillVouchers seckillVouchers = BeanUtil.mapToBean(seckillVoucherMaps, SeckillVouchers.class, true, null);
        AssertUtil.isTrue(seckillVouchers == null, "该代金券并未有抢购活动");
        // 判断是否有效
        AssertUtil.isTrue(seckillVouchers.getIsValid() == 0, "该活动已结束");
        // 判断是否开始、结束
        Date now = new Date();
        AssertUtil.isTrue(now.before(seckillVouchers.getStartTime()), "该抢购还未开始");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "该抢购已结束");
        // 判断是否卖完
        AssertUtil.isTrue(seckillVouchers.getAmount() < 1, "该券已经卖完了");
        // 判断登录用户是否已抢到(一个用户针对这次活动只能买一次)
        // 这里没有加锁，因为是线程不安全的
        // FkVoucherId 代金券id
        VoucherOrders order = voucherOrdersMapper.findDinerOrder(userId,
                seckillVouchers.getFkVoucherId());
        AssertUtil.isTrue(order != null, "该用户已抢到该代金券，无需再抢");

        // 使用 redis锁一个账号只能买一个
        String lockName = RedisKeyConstant.lock_key.getKey() + userId + ":" + voucherId;
        long expireTime = seckillVouchers.getEndTime().getTime() - now.getTime();
        // 锁
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLock = lock.tryLock(expireTime, TimeUnit.MILLISECONDS);
            if (isLock) {
                // ----------采用 Redid + Lua 解决问题 ----------
                List<Object> keys = new ArrayList<>();
                keys.add(redisKey);
                keys.add("\"amount\"");
                Long amount = redissonClient.getScript().eval(RScript.Mode.READ_ONLY, STOCK_SCRIPT, RScript.ReturnType.INTEGER, keys);
                // 下单 这里没有加锁，因为是线程不安全的
                VoucherOrders voucherOrders = new VoucherOrders();
                voucherOrders.setFkDinerId(userId);
                voucherOrders.setFkVoucherId(seckillVouchers.getFkVoucherId());
                String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
                voucherOrders.setOrderNo(orderNo);
                voucherOrders.setOrderType(1);
                voucherOrders.setStatus(0);
                amount = (long) voucherOrdersMapper.save(voucherOrders);
                AssertUtil.isTrue(amount == 0, "用户抢购失败");
            }
        } catch (Exception e) {
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 解锁
            lock.unlock();
            e.printStackTrace();
        }
        return ResultInfoUtil.buildSuccess(path, "抢购成功");
    }

}
