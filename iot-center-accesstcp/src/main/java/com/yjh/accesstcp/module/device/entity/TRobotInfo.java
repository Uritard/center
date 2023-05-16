package com.yjh.accesstcp.module.device.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 机器人表
 * @TableName t_robot_info
 */
@Data
public class TRobotInfo implements Serializable {
    /**
     * 机器人id
     */
    private Long robotId;

    /**
     * 
     */
    private String robotCode;

    /**
     * 机器人/无人机编号编码
     */
    private String robotNum;

    /**
     * 机器人名称
     */
    private String robotName;

    /**
     * 机巢名称
     */
    private String nestName;

    /**
     * 机巢编码
     */
    private String nestCode;

    /**
     * 机器人在线状态，1-在线0-离线
     */
    private String robotStatus;

    /**
     * 机器人型号
     */
    private Integer robotType;

    /**
     * 无人机型号
     */
    private Integer droneType;

    /**
     * 机器人ip
     */
    private String robotIp;

    /**
     * 机器人端口
     */
    private Integer robotPort;

    /**
     * 可见光IP
     */
    private String lightIp;

    /**
     * 可见光端口号
     */
    private String lightPort;

    /**
     * 可见光-用户名
     */
    private String identityManager;

    /**
     * 可见光-密码
     */
    private String identityCode;

    /**
     * 红外IP
     */
    private String lnferadIp;

    /**
     * 红外端口号
     */
    private Integer inferadPort;

    /**
     * 红外-用户名
     */
    private String inferadUsername;

    /**
     * 红外-密码
     */
    private String inferadPassword;

    /**
     * 照片路径
     */
    private String photePath;

    /**
     * 
     */
    private String createBy;

    /**
     * 
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 
     */
    private String updateBy;

    /**
     * 
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;

    /**
     * 机器人厂家
     */
    private String robotFactory;

    /**
     * 生成国家
     */
    private String madeIn;

    /**
     * 出厂日期
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date madeDate;

    /**
     * 使用状态，1-已报废2-使用中3-未使用
     */
    private String isUse;

    /**
     * 投运日期
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date commissionDate;

    /**
     * 上层区域id
     */
    private Long upRegionId;

    /**
     * 机器人类型.1-室内0-室外-2-轨道
     */
    private String robotPosition;

    /**
     * 无人机类型
     */
    private String dronePosition;

    /**
     * 设备来源
     */
    private String robotSource;

    /**
     * 安装位置
     */
    private String address;

    /**
     * 使用单位
     */
    private String buildingUser;

    /**
     * 出场编号
     */
    private String appearanceNumber;

    /**
     * 缺陷记录
     */
    private String defectRecord;

    /**
     * 大修记录
     */
    private String repairRecord;

    /**
     * 退出再重放记录
     */
    private String exitPutintoRecord;

    /**
     * 
     */
    private String remarks;

    /**
     * 录像机id
     */
    private Long recordId;

    /**
     * 通道号可见光
     */
    private Integer channelNumLight;

    /**
     * 通道号红外
     */
    private Integer channelNumInferad;

    /**
     * 上次登录时间(毫秒数)
     */
    private Long lastOnlineTime;

    /**
     * 在线时长累积(毫秒)
     */
    private Long duration;

    /**
     * 离线次数
     */
    private Long offLineCount;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    /**
     * 机器人地图图片尺寸
     */
    private String imageSize;

    private static final long serialVersionUID = 1L;
}