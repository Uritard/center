package com.yjh.accesstcp.module.device.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标准区域表
 * @TableName t_std_region
 */
@Data
public class TStdRegion implements Serializable {
    /**
     * 区域ID
     */
    private Long regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 区域类型（1:国家,2:省份、直辖市,3:运维站,4:变电站,5:间隔,6:设备,7:部位）
     */
    private Integer sort;

    /**
     * 上级区域ID
     */
    private Long upRegionId;

    /**
     * 区域ID层级
     */
    private String upRegionIds;

    /**
     * 下级节点ID(state为0时有值)
     */
    private String regionCode;

    /**
     * 下级节点区域ID(state为0时有值)
     */
    private String originRegionId;

    /**
     * 变电站ID
     */
    private String stationId;

    /**
     * 场站名称
     */
    private String stationName;

    /**
     * 0:非当前变电站 1：当前变电站
     */
    private Integer state;

    /**
     * 边缘节点在线状态
     */
    private String edgeStatus;

    /**
     * 站所地图路径
     */
    private String regionPath;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}