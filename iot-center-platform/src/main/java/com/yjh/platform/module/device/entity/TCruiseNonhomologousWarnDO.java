package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/3
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "tCruiseNonhomologousWarnDO", description = "非同源告警表")
public class TCruiseNonhomologousWarnDO  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=60)
    @ApiModelProperty(value = "非同源巡检点实例ID")
    @TableField(value = "warn_id",updateStrategy = FieldStrategy.IGNORED)
    private String warnId;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联告警规则id")
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    /**
     * NonhomologousWarnEnum
     *
     */
    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联告警类型")
    @TableField(value = "warn_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer warnType;

    @Max(value=32)
    @ApiModelProperty(value = "告警内容")
    @TableField(value = "warn_content",updateStrategy = FieldStrategy.IGNORED)
    private String warnContent;

    @Length(max = 32,message = "告警时间")
    @ApiModelProperty(value = "告警时间")
    @TableField(value = "warn_time",updateStrategy = FieldStrategy.IGNORED)
    private String warnTime;


    private String oneCruiseDeviceName;
    private String twoCruiseDeviceName;
    private String threeCruiseDeviceName;

}
