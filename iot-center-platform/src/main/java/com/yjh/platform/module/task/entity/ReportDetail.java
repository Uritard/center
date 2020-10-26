package com.yjh.platform.module.task.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ReportDetail", description = "报表导出实体类")
public class ReportDetail implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @ApiModelProperty(value = "巡视任务结果id")
    private String cruiseResultId;

    @ApiModelProperty(value = "状态 -1数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    private Integer state;

    @ApiModelProperty(value = "实际结果")
    private Integer jieGuo;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "实际结果")
    private String identifyResult;

    @ApiModelProperty(value = "巡检点实例名称")
    private String instanceName;

    @ApiModelProperty(value = "巡检分析图片")
    @Excel(name = "图片路径", type = 2 ,width = 40 , height = 20,imageType = 2)
    private Object picture;

    @ApiModelProperty(value = "巡检分析图片路径")
    private String picpath;

    @ApiModelProperty(value = "巡检结果")
    private String cruiseResult;

    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;

}