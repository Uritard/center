package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2022/4/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视设备模型", description = "机器人、无人机等")
public class PatrolDeviceModel {

    private String patroldevcieName;//设备名称
    private String patroldevcieCode;//设备编码
    private String stationCode;//变电站编码
    private String deviceModel;//设备型号
    private String manufacturer;//生产厂家
    private String useUnit;//使用单位
    private String deviceSource;//设备来源
    private String productionDate;//生产日期
    private String productionCode;//出厂编号
    private String istransport;//是否轮转
    private String useMode;//使用类型
    private String video_mode;//视频类型
    private String place;//安装位置
    private String type;//设备类型
    private String patroldevcie_info;//备注信息
    private String robots_code;//所属机器人
}
