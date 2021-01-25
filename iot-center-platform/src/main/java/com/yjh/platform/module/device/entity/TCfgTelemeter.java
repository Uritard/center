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
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgTelemeter对象", description = "遥测量表")
public class TCfgTelemeter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 20,message = "deviceId长度必须小于等于20")
    @ApiModelProperty(value = "设备编号")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private String deviceId;

    @Length(max = 20,message = "meteId长度必须小于等于20")
    @ApiModelProperty(value = "监控量编号")
    @TableField(value = "mete_id",updateStrategy = FieldStrategy.IGNORED)
    private String meteId;

    @Length(max = 256,message = "meteName长度必须小于等于256")
    @ApiModelProperty(value = "监控量名称")
     @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效上限")
    @TableField(value = "up_effect",updateStrategy = FieldStrategy.IGNORED)
    private Float upEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效下限")
     @TableField(value = "down_effect",updateStrategy = FieldStrategy.IGNORED)
    private Float downEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "小数点后的有效位数")
    private Integer metePrecision;

    @Length(max = 16,message = "unit长度必须小于等于16")
    @ApiModelProperty(value = "单位")
     @TableField(value = "unit",updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @Max(value=999999999)
    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @Max(value=999999999)
    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @Max(value=999999999)
    @ApiModelProperty(value = "上下限带宽")
    @TableField(value = "limit_band",updateStrategy = FieldStrategy.IGNORED)
    private Float limitBand;

    @Max(value=999999999)
    @ApiModelProperty(value = "变化幅度门限")
     @TableField(value = "change_limit",updateStrategy = FieldStrategy.IGNORED)
    private Float changeLimit;

    @Length(max = 256,message = "validMid长度必须小于等于256")
    @ApiModelProperty(value = "有效性表达式变量串")
    @TableField(value = "valid_mid",updateStrategy = FieldStrategy.IGNORED)
    private String validMid;

    @Length(max = 256,message = "validString长度必须小于等于256")
    @ApiModelProperty(value = "有效性判断表达式")
    @TableField(value = "valid_string",updateStrategy = FieldStrategy.IGNORED)
    private String validString;

    @Max(value=999999999)
    @ApiModelProperty(value = "无效时的值")
    @TableField(value = "invalid_value",updateStrategy = FieldStrategy.IGNORED)
    private Float invalidValue;

    @Max(value=999999999)
    @ApiModelProperty(value = "当前值")
     @TableField(value = "last_value",updateStrategy = FieldStrategy.IGNORED)
    private Float lastValue;

    @Past
    @ApiModelProperty(value = "当前值更新时间")
     @TableField(value = "last_time",updateStrategy = FieldStrategy.IGNORED)
    private Date lastTime;

    @Length(max = 20,message = "meteCode长度必须小于等于20")
    @ApiModelProperty(value = "信号标准化编码")
    @TableField(value = "mete_code",updateStrategy = FieldStrategy.IGNORED)
    private String meteCode;

    @Length(max = 20,message = "deviceType长度必须小于等于20")
    @ApiModelProperty(value = "设备类型")
    @TableField(value = "device_type",updateStrategy = FieldStrategy.IGNORED)
    private String deviceType;

    @Length(max = 512,message = "description长度必须小于等于512")
    @ApiModelProperty(value = "监控量描述")
    @TableField(value = "description",updateStrategy = FieldStrategy.IGNORED)
    private String description;

    @Max(value=999999999)
    @ApiModelProperty(value = "一级告警上限")
    @TableField(value = "hilimit1",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit1;

    @Max(value=999999999)
    @ApiModelProperty(value = "一级告警下限")
    @TableField(value = "lolimit1",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit1;

    @Max(value=999999999)
    @ApiModelProperty(value = "二级告警上限")
    @TableField(value = "hilimit2",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit2;

    @Max(value=999999999)
    @ApiModelProperty(value = "二级告警下限")
    @TableField(value = "lolimit2",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit2;

    @Max(value=999999999)
    @ApiModelProperty(value = "三级告警上限")
    @TableField(value = "hilimit3",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit3;

    @Max(value=999999999)
    @ApiModelProperty(value = "三级告警下限")
    @TableField(value = "lolimit3",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit3;

    @Max(value=999999999)
    @ApiModelProperty(value = "四级告警上限")
    @TableField(value = "hilimit4",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit4;

    @Max(value=999999999)
    @ApiModelProperty(value = "四级告警下限")
    @TableField(value = "lolimit4",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit4;

    @Max(value=999999999)
    @ApiModelProperty(value = "标称值")
    @TableField(value = "stander",updateStrategy = FieldStrategy.IGNORED)
    private Float stander;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "存储周期")
    @TableField(value = "storageperiod",updateStrategy = FieldStrategy.IGNORED)
    private Long storageperiod;

    @Length(max = 512,message = "linkMeteId长度必须小于等于512")
    @ApiModelProperty(value = "关联的遥信量")
    @TableField(value = "link_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private String linkMeteId;


}
