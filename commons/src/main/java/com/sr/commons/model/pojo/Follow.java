package com.sr.commons.model.pojo;

import com.sr.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "食客关注实体类")
@Getter
@Setter
public class Follow extends BaseModel {

    @ApiModelProperty("用户ID")
    private int dinerId;
    @ApiModelProperty("关注用户ID")
    private Integer followDinerId;

}