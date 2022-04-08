package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2022-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDroneInfo对象", description = "无人机表")
public class TDroneInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "无人机id")
    private Long droneId;

    @ApiModelProperty(value = "无人机编码")
    private String droneCode;

    @ApiModelProperty(value = "无人机名称")
    private String droneName;

    @ApiModelProperty(value = "机巢ID")
    private Long nestId;

    @ApiModelProperty(value = "无人机在线状态，1-在线0-离线2-新建")
    private String droneState;

    @ApiModelProperty(value = "无人机型号")
    private Integer droneType;

    @ApiModelProperty(value = "无人机ip")
    private String droneIp;

    @ApiModelProperty(value = "无人机端口")
    private Integer dronePort;

    @ApiModelProperty(value = "无人机厂家")
    private String droneFactory;

    @ApiModelProperty(value = "使用状态，1-已报废2-使用中3-未使用")
    private String isUse;

    @ApiModelProperty(value = "投运日期")
    private Date commissionDate;

    @ApiModelProperty(value = "上层区域id")
    private Integer upRegionId;

    @ApiModelProperty(value = "站所编码")
    private String stationCode;

    private String stationName;

    @ApiModelProperty(value = "无人机类型.0-固定翼1-旋翼-2-无人艇3-伞翼4-扑翼")
    private String dronePosition;

    @ApiModelProperty(value = "设备来源")
    private String droneSource;

    @ApiModelProperty(value = "安装位置")
    private String address;

    @ApiModelProperty(value = "使用单位")
    private String buildingUser;

    @ApiModelProperty(value = "出场编号")
    private String appearanceNumber;

    @ApiModelProperty(value = "缺陷记录")
    private String defectRecord;

    @ApiModelProperty(value = "大修记录")
    private String repairRecord;

    @ApiModelProperty(value = "退出再投放记录")
    @TableField("exit_putInto_record")
    private String exitPutintoRecord;

    private Date createTime;

    private Date updateDate;

    private String remarks;


}
