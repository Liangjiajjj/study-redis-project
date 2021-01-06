package com.sr.scekill.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.sr.commons.model.domain.ResultInfo;
import com.sr.commons.model.pojo.SeckillVouchers;
import com.sr.commons.model.pojo.VoucherOrders;
import com.sr.commons.model.vo.SignInDinerInfo;
import com.sr.commons.utils.AssertUtil;
import com.sr.commons.utils.ResultInfoUtil;
import com.sr.scekill.mapper.SeckillVouchersMapper;
import com.sr.scekill.mapper.VoucherOrdersMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedHashMap;

@Service
public class SeckillService {

    @Resource
    private SeckillVouchersMapper seckillVouchersMapper;
    @Resource
    private VoucherOrdersMapper voucherOrdersMapper;
    @Resource
    private RestTemplate restTemplate;

    @Transactional(rollbackFor = Exception.class) // 事务是否回退
    // 添加代金券
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
        SeckillVouchers vouchersFormDB = seckillVouchersMapper.selectVoucher(seckillVouchers.getFkVoucherId());
        AssertUtil.isTrue(vouchersFormDB != null, "该卷已经存在");
        seckillVouchersMapper.save(seckillVouchers);
    }


    // 客户端抢代金券（基于数据库）
    public ResultInfo doSeckillOfMySQL(Integer voucherId, int userId, String path) {
        // 基本参数校验
        AssertUtil.isTrue(voucherId == null || voucherId < 0, "请选择需要抢购的代金券");
        // 判断此代金券是否加入抢购
        SeckillVouchers seckillVouchers = seckillVouchersMapper.selectVoucher(voucherId);
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
        VoucherOrders order = voucherOrdersMapper.findDinerOrder(userId,
                seckillVouchers.getId());
        AssertUtil.isTrue(order != null, "该用户已抢到该代金券，无需再抢");
        // 扣库存
        int count = seckillVouchersMapper.stockDecrease(seckillVouchers.getId());
        AssertUtil.isTrue(count == 0, "该券已经卖完了");
        // 下单
        VoucherOrders voucherOrders = new VoucherOrders();
        voucherOrders.setFkDinerId(userId);
        voucherOrders.setFkSeckillId(seckillVouchers.getId());
        voucherOrders.setFkVoucherId(seckillVouchers.getFkVoucherId());
        String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
        voucherOrders.setOrderNo(orderNo);
        voucherOrders.setOrderType(1);
        voucherOrders.setStatus(0);
        count = voucherOrdersMapper.save(voucherOrders);
        AssertUtil.isTrue(count == 0, "用户抢购失败");
        return ResultInfoUtil.buildSuccess(path, "抢购成功");
    }

}
