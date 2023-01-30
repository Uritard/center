package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author lqh
 * @since 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmConfBak对象", description = "算法配置表2")
public class TAlgorithmConfBak implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备标准测点ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Max(value=999999999999999999l)
     @TableField(value = "algorithm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;

    @Length(max = 68,message = "configName长度必须小于等于68")
    @ApiModelProperty(value = "算法配置名称")
     @TableField(value = "config_name",updateStrategy = FieldStrategy.IGNORED)
    private String configName;

    @Max(value=999999999)
    @ApiModelProperty(value = "状态")
    private Integer status;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否删除")
    private Integer ifDel;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否展示1展示，2不展示")
    private Integer ifShow;

    @Length(max = 255,message = "picUrl长度必须小于等于255")
    @ApiModelProperty(value = "图标路径")
     @TableField(value = "pic_url",updateStrategy = FieldStrategy.IGNORED)
    private String picUrl;

    @Max(value=999999999)
    @ApiModelProperty(value = "0不应用，1应用到日常巡视，2..待定")
    private Integer applyModule;


    @ApiModelProperty(value = "创建时间")
    private Date createTime;


    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    private String edgeCode;

    private Integer pageNum=1;

    private Integer pageSize=0;

}
