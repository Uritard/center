package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import java.util.List;

/**
 * @author lqh
 * @since 2020/9/3
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象扩充", description = "标准设备测点表扩充")
public class TStdDeviceMeteDetail extends TStdDeviceMete{



    //private String meteKindName;

    private String unitName;
    @TableField(value = "custom_id",updateStrategy = FieldStrategy.IGNORED)
    private String customType;

    private String customTypeName;

    private String deviceName;

    private String alarmTypeName;

    private String meteTypeName;

    private String alarmLevelName;

    private Long upRegionId;
    private List<Long> ids;

    private String meterTypeName;

    @Max(value=999999999)
    private Integer analyseType;
    private String analyseTypeName;
    private String isAi;//是否配置缺陷算法
    private String isJudge;//是否配置判别算法

    private Integer isRedundant;//是否冗余配置
    private String rules;//告警规则

}

