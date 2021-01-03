package com.sr.scekill.controller;

import com.sr.commons.model.domain.ResultInfo;
import com.sr.commons.model.pojo.SeckillVouchers;
import com.sr.commons.utils.ResultInfoUtil;
import com.sr.scekill.service.SeckillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

}
