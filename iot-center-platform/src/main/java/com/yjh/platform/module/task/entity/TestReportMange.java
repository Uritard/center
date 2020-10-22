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

    @Excel(name = "啥也不是", orderNum = "1",width = 20)
    private Integer defectLevel;
    @Excel(name = "啥也不是", orderNum = "2",width = 20)
    private String defectLevelName;
    @Excel(name = "啥也不是",format  = "yyyy-MM-dd HH:mm:ss", orderNum = "3",width = 20)
    private Date defectTime;
    @Excel(name = "啥也不是", orderNum = "4",width = 20)
    private String defectContent;
    @Excel(name = "啥也不是", orderNum = "5",width = 20)
    private Integer confMode;
    @Excel(name = "啥也不是", orderNum = "6",width = 20)
    private String confModeName;
    @Excel(name = "啥也不是", orderNum = "7",width = 20)
    private Integer dealType;
    @Excel(name = "啥也不是", orderNum = "8",width = 20)
    private String dealTypeName;
    @Excel(name = "啥也不是", orderNum = "9",width = 20)
    private String dealInfo;
    @Excel(name = "啥也不是",format  = "yyyy-MM-dd HH:mm:ss", orderNum = "10",width = 20)
    private Date dealTime;
    @Excel(name = "啥也不是", orderNum = "11",width = 20)
    private String imagePath;
    @Excel(name = "啥也不是", orderNum = "12",width = 20)
    private String videoPath;

}
