package com.yjh.accessrobot.module.command.dao;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.module.command.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TRobotInfoDao {

    int insert(TRobotInfo tRobotInfo);

    int deleteByPrimaryId(@Param(value = "robotId") Long robotId);

    int update(TRobotInfo tRobotInfo);

    TRobotInfo selectByPrimaryId(@Param(value = "robotId") Long robotId);

    List<TRobotInfo> select(@Param(value = "robotId") Long robotId,
                            @Param(value = "robotCode") String robotCode,
                            @Param(value = "robotName") String robotName,
                            @Param(value = "robotStatus") String robotStatus,
                            @Param(value = "robotType") Integer robotType,
                            @Param(value = "robotIp") String robotIp,
                            @Param(value = "robotPort") Integer robotPort,
                            @Param(value = "lightIp") String lightIp,
                            @Param(value = "lightPort") String lightPort,
                            @Param(value = "identityManager") String identityManager,
                            @Param(value = "identityCode") String identityCode,
                            @Param(value = "lnferadIp") String lnferadIp,
                            @Param(value = "inferadPort") Integer inferadPort,
                            @Param(value = "inferadUsername") String inferadUsername,
                            @Param(value = "inferadPassword") String inferadPassword,
                            @Param(value = "photePath") String photePath,
                            @Param(value = "createBy") String createBy,
                            @Param(value = "createDate") Date createDate,
                            @Param(value = "updateBy") String updateBy,
                            @Param(value = "updateDate") Date updateDate,
                            @Param(value = "robotFactory") String robotFactory,
                            @Param(value = "isUse") String isUse,
                            @Param(value = "commissionDate") Date commissionDate,
                            @Param(value = "upRegionId") Long upRegionId,
                            @Param(value = "robotPosition") String robotPosition,
                            @Param(value = "remarks") String remarks);
    List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo);

    int batchInsert(List<TRobotInfo> list);

    /**
     * 更新所有的机器人状态，将其设置为离线
     * @param robotStatus 机器人状态
     * @return int
     */
    int updateAllRobotStatus(@Param(value = "robotStatus") String robotStatus);
    /**
     * 查询表中所有机器人编码
     * @return List<String>
     */
    List<String> selectAllRobotCode();
    /**
     * 查询所有在线的机器人
     * @return List<String>
     */
    List<String> selectOnline();
    /**
     * 根据机器人id查询机器人编码
     * @param robotCode 机器人id
     * @return Long
     */
    Long selectRobotIdByCode(@Param(value = "robotCode") String robotCode);
    /**
     * 根据机器人编码查询机器人名称
     * @param robotCode 机器人编码
     * @return String
     */
    String selectRobotNameByCode(@Param(value = "robotCode") String robotCode);
    /**
     * 查询字典id
     * @param dictNote 字典编码
     * @param colName 字典列名
     * @return String
     */
    String selectDictCodeByNote(@Param(value = "dictNote")String dictNote,
                                @Param(value = "colName")String colName);
    /**
     * 根据机器人编码查询在线状态
     * @param robotCode 机器人编码
     * @return String
     */
    String selectStatusByRobotCode(@Param(value = "robotCode") String robotCode);
    /**
     * 判断任务是否为联动任务
     * @param unionId 联合任务id
     * @return Integer
     */
    Integer selectIsUnionTask(@Param(value = "unionId") String unionId);
    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     * @param taskId 任务id
     * @return List<Long>
     */
    List<Long> selectInstanceForTaskGoOn(@Param(value = "taskId") String taskId);
    /**
     * 删除巡视点
     * @param instanceId 巡检点id
     * @return int
     */
    int deleteInstanceId(@Param(value = "instanceId")Long instanceId);
    /**
     * 根据巡视点查询机器人测点id
     * @param instanceId 巡视点id
     * @return String
     */
    String selectInspectionCode(@Param(value = "instanceId") Long instanceId);
    /**
     * 根据任务id查询已组装的任务结果
     * @param taskId 任务id
     * @return TCruiseResult
     */
    TCruiseResult selectTaskResultId(@Param(value = "taskId") String taskId);
    /**
     * 根据任务id查询任务数据
     * @param taskId 任务id
     * @return TCruiseTask
     */
    TCruiseTask selectTCruiseTask(@Param(value = "taskId") String taskId);
    /**
     * 插入机器人本体告警数据
     * @param tRobotAlarm 机器人本体告警
     * @return int
     */
    int insertRobotAlarm(TRobotAlarm tRobotAlarm);
    /**
     * 插入巡视任务结果数据
     * @param list 机器人巡视点结果集
     * @return int
     */
    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);
    /**
     * 插入巡视任务结果数据
     * @param list 机器人巡视点结果集
     * @return int
     */
    int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> list);
    /**
     * 更新任务结果表数据
     * @param tCruiseResult 任务结果数据
     * @return int
     */
    int updateTCruiseResult(TCruiseResult tCruiseResult);
    /**
     * 插入巡视任务结果数据
     * @param tCruiseTaskResult 任务结果数据
     * @return int
     */
    int insertTCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult);
    /**
     * 查询机器人测点配置算法情况
     * @param inspectionCode 机器人测点id
     * @return List<AlgorithmDeviceMete>
     */
    List<AlgorithmDeviceMete> selectAlgorithm(@Param(value = "inspectionCode") String inspectionCode);
    /**
     * 根据任务id查询检修区域的点
     * @param taskId 任务id
     * @return List<TCruiseTaskResultDetail>
     */
    List<TCruiseTaskResultDetail> selectRepairTCTRDList(@Param(value = "taskId")String taskId);
    /**
     * 根据任务id查询检修区域的点
     * @param taskId 任务id
     * @return List<TCruiseDataResult>
     */
    List<TCruiseDataResult> selectRepairTCDRList(@Param(value = "taskId")String taskId);
    /**
     * 根据巡视点查询相关信息
     * @param instanceId 巡视点id
     * @return TCruisePointInstanceDetail
     */
    TCruisePointInstanceDetail selectForTask(@Param(value = "instanceId")Long instanceId);
    /**
     * 根据巡视点id查询算法配置情况
     * @param deviceMeteId 测点id
     * @return List<TAlgorithmInfo>
     */
    List<TAlgorithmInfo> selectByDeviceMeteId(@Param(value = "deviceMeteId") Long deviceMeteId);
    /**
     * 根据测点id查询测点信息
     * @param deviceMeteId 测点id
     * @return TStdDeviceMete
     */
    TStdDeviceMete selectDeviceMete(@Param(value = "deviceMeteId") Long deviceMeteId);
    /**
     * 查询站所信息
     * @return TStdRegion
     */
    TStdRegion selectTSRegionForStation(@Param(value = "robotId") Long robotId);
    /**
     * 批量插入巡视点
     * @param list 巡视点id集
     * @return int
     */
    int batchInsertInstance(List<TCruisePointInstance> list);
    /**
     * 插入预案与任务关联表
     * @param list 巡视点id集
     * @return int
     */
    int batchInsertTaskAttr(List<TCruiseTaskAttr> list);
    /**
     * 插入任务结果信息
     * @param tCruiseResult 任务结果信息
     * @return int
     */
    int insertTCruiseResult(TCruiseResult tCruiseResult);
    /**
     * 批量插入任务
     * @param list 任务集
     * @return int
     */
    int batchInsertTask(List<TCruiseTask> list);
    /**
     * 插入机器人巡检点告警及分析机器人巡检点结果后的告警
     * @param warnInfo 告警信息
     * @return int
     */
    int insertWarn(TWarnInfo warnInfo);
    /**
     * 根据任务id和巡检点id查询告警id
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @return Long
     */
    Long selectWarnId(@Param(value = "taskId")String taskId,
                      @Param(value = "instanceId")Long instanceId);
    /**
     * 根据任务id和巡检点id查询告警id查询该点是否为告警
     * @param instanceId 巡视点id
     * @param taskId 任务id
     * @return int
     */
    int selectIsWarn(@Param(value = "instanceId")Long instanceId,@Param(value = "taskId")String taskId);
    /**
     * 更新任务结果表
     * @param cruiseResultId 巡视点结果id
     * @return int
     */
    int updateIsWarn(@Param(value = "cruiseResultId")String cruiseResultId);
    /**
     * 根据巡视点id查询测点信息
     * @param instanceId 巡视点id
     * @return TStdDeviceMete
     */
    TStdDeviceMete selectDeviceMeteInfo(@Param(value = "instanceId")Long instanceId);
    /**
     * 判断任务是否属于机器人本体任务
     * @param taskId 任务id
     * @return Long
     */
    Long selectIsRobotTask(@Param(value = "taskId")String taskId);
    /**
     * 查询站端的任务在巡视主机上未完成的巡视点
     * @param robotId
     * @return List<String>
     */
    List<String> HasStandTaskIsFinish(@Param(value = "robotId")Long robotId);
    /**
     * 更新站端任务状态
     * @param taskId 任务id
     * @param state 状态
     * @return int
     */
    int updateStandTaskStatus(@Param(value = "taskId")String taskId,@Param(value = "state")Integer state);

    /**
     * 新增环控告警
     */
    int insertEnv(Map<String, String> envWarn);

    /**
     * 根据 设备机器人id，查询上级区域
     * @date 2022
     */
    String selectRegionIdByrobotId(@Param(value = "robotCode")String robotCode);

    int updateTCruiseTask(Map<String, Object> taskMap);

    List<String> selectCameraUrlByDeviceId(@Param(value = "deviceId") String deviceId);

    String selectDeviceTypeName(@Param(value = "deviceId") String deviceId);

}
