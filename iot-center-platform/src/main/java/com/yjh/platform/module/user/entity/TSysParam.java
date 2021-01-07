package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TSysParam对象", description = "系统参数表")
public class TSysParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "参数ID")
    @TableId(value = "param_id", type = IdType.AUTO)
    private Integer paramId;

    @ApiModelProperty(value = "参数编码")
    private String paramCode;

    @ApiModelProperty(value = "参数类型")
    private Integer paramType;

    private String paramTypeName;

    @ApiModelProperty(value = "参数名称")
    private String paramName;

    @ApiModelProperty(value = "参数内容")
    private String content;

    @ApiModelProperty(value = "描述")
    private String remark;


}
