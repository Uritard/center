package com.yjh.platform.module.task.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/10/20 - 16:27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TestReportMange对象", description = "报表导出测试表")
public class TestReportMange implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "缺陷等级", orderNum = "1",width = 20)
    private Integer defectLevel;
    @Excel(name = "缺陷时间", orderNum = "2",width = 20)
    private Date defectTime;
    @Excel(name = "缺陷内容", orderNum = "3",width = 20)
    private String defectContent;
    @Excel(name = "缺陷状态", orderNum = "4",width = 20)
    private Integer confMode;
    @Excel(name = "处理方式", orderNum = "5",width = 20)
    private Integer dealType;
    @Excel(name = "处理意见", orderNum = "6",width = 20)
    private String dealInfo;
    @Excel(name = "确认时间", orderNum = "7",width = 20)
    private Date dealTime;
    @Excel(name = "图片地址", orderNum = "8",width = 20)
    private String imagePath;
    @Excel(name = "视频地址", orderNum = "9",width = 20)
    private String videoPath;

}
