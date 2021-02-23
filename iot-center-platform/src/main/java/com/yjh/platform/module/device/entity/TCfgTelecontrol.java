package com.yjh.platform.module.device.entity;

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

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgTelecontrol对象", description = "遥控量表")
public class TCfgTelecontrol implements Serializable {

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
    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @Max(value=999999999)
    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @Max(value=999999999)
    @ApiModelProperty(value = "可控状态")
    private Integer controlStatus;

    @Length(max = 256,message = "enableString长度必须小于等于256")
    @ApiModelProperty(value = "控制使能条件表达式")
    @TableField(value = "enable_string",updateStrategy = FieldStrategy.IGNORED)
    private String enableString;

    @Length(max = 256,message = "succeedString长度必须小于等于256")
    @ApiModelProperty(value = "控制成功条件表达式")
     @TableField(value = "succeed_string",updateStrategy = FieldStrategy.IGNORED)
    private String succeedString;

    @Length(max = 256,message = "triggerString长度必须小于等于256")
    @ApiModelProperty(value = "触发条件表达式")
    @TableField(value = "trigger_string",updateStrategy = FieldStrategy.IGNORED)
    private String triggerString;

    @Max(value=999999999)
    @ApiModelProperty(value = "控制参数")
    private Integer controlValue;

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

    @Length(max = 512,message = "describer长度必须小于等于512")
    @ApiModelProperty(value = "态值描述")
    @TableField(value = "describer",updateStrategy = FieldStrategy.IGNORED)
    private String describer;
    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
