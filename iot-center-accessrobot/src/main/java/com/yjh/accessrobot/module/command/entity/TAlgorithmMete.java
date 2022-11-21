package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 算法测点配置表
 * @TableName t_algorithm_mete
 */
@Data
public class TAlgorithmMete implements Serializable {
    /**
     * 设备标准测点ID
     */
    private Long deviceMeteId;

    /**
     * 
     */
    private Long algorithmId;

    /**
     * 算法配置名称
     */
    private String configName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否删除
     */
    private Integer ifDel;

    /**
     * 是否展示1展示，2不展示
     */
    private Integer ifShow;

    /**
     * 图标路径
     */
    private String picUrl;

    /**
     * 0不应用，1应用到日常巡视，2..待定
     */
    private Integer applyModule;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始Id
     */
    private String originId;

    private static final long serialVersionUID = 1L;

}