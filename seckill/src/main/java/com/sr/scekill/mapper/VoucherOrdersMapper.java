package com.sr.scekill.mapper;

import com.sr.commons.model.pojo.VoucherOrders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 代金券订单 Mapper
 */
public interface VoucherOrdersMapper {

    // 根据食客 ID 和秒杀 ID 查询代金券订单
    @Select("select id, order_no, fk_voucher_id, fk_diner_id, qrcode, payment," +
            " status, fk_seckill_id, order_type, create_date, update_date, " +
            " is_valid from t_voucher_orders where fk_diner_id = #{dinerId} " +
            " and fk_seckill_id = #{seckillId} and is_valid = 1 and status > -1  ")
    VoucherOrders findDinerOrder(@Param("dinerId") Integer dinerId,
                                 @Param("seckillId") Integer seckillId);

    // 新增代金券订单
    @Insert("insert into t_voucher_orders (order_no, fk_voucher_id, fk_diner_id, " +
            " status, fk_seckill_id, order_type, create_date, update_date,  is_valid)" +
            " values (#{orderNo}, #{fkVoucherId}, #{fkDinerId}, #{status}, #{fkSeckillId}, " +
            " #{orderType}, now(), now(), 1)")
    int save(VoucherOrders voucherOrders);

}
