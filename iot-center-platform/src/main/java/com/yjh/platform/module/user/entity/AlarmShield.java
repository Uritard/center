package com.yjh.platform.module.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.device.entity.TStdDeviceMeteDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 告警屏蔽配置
 * </p>
 *
 * @author lqh
 * @since 2023-06-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AlarmShield对象", description="告警屏蔽配置")
public class AlarmShield implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "如果是巡视设备，就是巡视设备id，如机器人id。如果是被巡视设备，就是测点id。")
    private String shieldId;

    @ApiModelProperty(value = "名称")
    private String shieldName;

    @ApiModelProperty(value = "测点的巡视类型")
    private Integer meteType;

    private String meteTypeName;

    @ApiModelProperty(value = "测点的巡视小类型")
    private Integer meterType;

    private String meterTypeName;

    @ApiModelProperty(value = "是否启用 1-已起用 0-未启用")
    private Integer enable = 1;

    @ApiModelProperty(value = "创建屏蔽的用户id")
    private Long createUserId;

    @ApiModelProperty(value = "用户名称")
    private String userName;

    @ApiModelProperty(value = "屏蔽结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty(value = "屏蔽类型：1-巡视设备，2-测点,3-巡视类型是")
    private Integer shieldType;

    @ApiModelProperty(value = "屏蔽的告警内容")
    private String warnContent;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private List<TStdDeviceMeteDetail> deviceMeteList;


}
