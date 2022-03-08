package com.yjh.platform.module.task.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.yjh.platform.common.utils.MyUrlImageConverter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
@Data
@ExcelIgnoreUnannotated //忽视无注解的属性
@ContentRowHeight(100)
@HeadRowHeight(20)
@ColumnWidth(20)
@Accessors(chain = true)
@ApiModel(value = "操作任务记录详情数据", description = "操作任务记录详情数据")
public class OperationTaskRecordResult {

    private String resultId;
    private String taskId;
    @ExcelProperty(value = "行为名称", index = 0)
    private String cruiseName;

    @ExcelProperty(value = "设备状态", index = 1)
    private String resultNum;

    @ApiModelProperty(value = "待确认图片")
    private String confirmPicpath;

    @ApiModelProperty(value = "结果图片")
    private String picpath;

    @ExcelProperty(value = "待确认图片",converter = MyUrlImageConverter.class, index = 2)
    private String origConfirmPicPath;

    @ExcelProperty(value = "结果图片",converter = MyUrlImageConverter.class, index = 3)
    private String origpic;

    @ExcelProperty(value = "操作时间", index = 4)
    private String startTime;

    @ExcelProperty(value = "操作人", index = 5)
    private String userName;

    @ExcelProperty(value = "工号", index = 6)
    private String userId;


}
