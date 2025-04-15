package com.yjh.platform.module.modelconf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 模型配置表
 * @author huyuhang
 * @TableName std_model_config
 */
@TableName(value ="std_model_config")
@Data
public class StdModelConfig implements Serializable {
    /**
     * 模型id
     */
    @TableId
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型状态;1：使用中  0：未使用
     */
    private Integer status;

    /**
     * 设备id
     */
    private Long droneId;

    /**
     * 配置项
     */
    private Object config;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createPerson;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
