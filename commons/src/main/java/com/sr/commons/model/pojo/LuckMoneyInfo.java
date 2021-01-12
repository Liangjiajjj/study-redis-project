package com.sr.commons.model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sr.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@ApiModel(description = "红包信息")
/**
 * 这个是用户真正的红包
 */
public class LuckMoneyInfo extends BaseModel {
    @ApiModelProperty("红包id")
    private Integer luckMoneyId;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("金额")
    private Long money;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;
    @ApiModelProperty("抢到时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date takeTime;
    @ApiModelProperty("抢到时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;
}
