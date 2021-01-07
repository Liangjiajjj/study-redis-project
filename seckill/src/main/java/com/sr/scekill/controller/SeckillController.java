package com.sr.scekill.controller;

import com.sr.commons.model.domain.ResultInfo;
import com.sr.commons.model.pojo.SeckillVouchers;
import com.sr.commons.utils.ResultInfoUtil;
import com.sr.scekill.service.SeckillService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
public class SeckillController {
    @Resource
    private SeckillService seckillService;
    @Resource
    private HttpServletRequest request;

    @PostMapping("add")
    public ResultInfo<String> addSeckillVouchers(@RequestBody SeckillVouchers seckillVouchers) {
        seckillService.addSeckillVouchers(seckillVouchers);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
    }

    @PostMapping("{voucherId}")
    public ResultInfo<String> doSeckill(@PathVariable Integer voucherId,  int userId) {
        ResultInfo resultInfo = seckillService.doSeckill(voucherId, userId, request.getServletPath());
        return resultInfo;
    }

    @PostMapping("test")
    public void test(){
        SeckillVouchers seckillVouchers = new SeckillVouchers();
        seckillService.addSeckillVouchers(seckillVouchers);
        seckillVouchers.setUpdateDate(new Date());
        seckillVouchers.setCreateDate(new Date());
        seckillVouchers.setIsValid(1);
        seckillVouchers.setAmount(10);
        seckillVouchers.setId(1);
        seckillService.addSeckillVouchers(seckillVouchers);
        for (int i = 0; i < 100; i++) {

        }
    }

}
