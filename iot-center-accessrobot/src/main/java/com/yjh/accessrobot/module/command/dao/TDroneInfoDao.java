package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TDroneInfoDao {

    int insert(TDroneInfo tDroneInfo);

    int deleteByPrimaryId(@Param(value = "droneId") Long droneId);

    int update(TDroneInfo tDroneInfo);

    TDroneInfo selectByPrimaryId(@Param(value = "droneId") Long droneId);

    List<TDroneInfo> select(@Param(value = "droneId") Long droneId,
                            @Param(value = "droneCode") String droneCode,
                            @Param(value = "droneName") String droneName,
//                            @Param(value = "nestId") Long nestId,
                            @Param(value = "droneState") String droneState,
                            @Param(value = "droneType") Integer droneType,
                            @Param(value = "droneIp") String droneIp,
                            @Param(value = "dronePort") Integer dronePort,
                            @Param(value = "droneFactory") String droneFactory,
                            @Param(value = "isUse") String isUse,
                            @Param(value = "commissionDate") Date commissionDate,
                            @Param(value = "upRegionId") Integer upRegionId,
                            @Param(value = "stationCode") String stationCode,
                            @Param(value = "stationName") String stationName,
                            @Param(value = "dronePosition") String dronePosition,
                            @Param(value = "droneSource") String droneSource,
                            @Param(value = "address") String address,
                            @Param(value = "buildingUser") String buildingUser,
                            @Param(value = "appearanceNumber") String appearanceNumber,
                            @Param(value = "defectRecord") String defectRecord,
                            @Param(value = "repairRecord") String repairRecord,
                            @Param(value = "exitPutintoRecord") String exitPutintoRecord,
                            @Param(value = "createTime") Date createTime,
                            @Param(value = "updateDate") Date updateDate,
                            @Param(value = "remarks") String remarks);
    List<TDroneInfo> selectByPage(TDroneInfo tDroneInfo);

    int batchInsert(List<TDroneInfo> list);

    /**
     * 更新所有的无人机状态，将其设置为离线
     * @param droneState 无人机状态
     * @return int
     */
    int updateAllDroneState(@Param(value = "droneState") String droneState);
    /**
     * 查询表中所有无人机编码
     * @return List<String>
     */
    List<String> selectAllDroneCode();
    /**
     * 查询所有在线的无人机
     * @return List<String>
     */
    List<String> selectOnline();
    /**
     * 根据无人机id查询无人机编码
     * @param droneCode 无人机id
     * @return Long
     */
    Long selectDroneIdByCode(@Param(value = "droneCode") String droneCode);
    /**
     * 根据无人机编码查询无人机名称
     * @param droneCode 无人机编码
     * @return String
     */
    String selectDroneNameByCode(@Param(value = "droneCode") String droneCode);
    /**
     * 查询字典id
     * @param dictNote 字典编码
     * @param colName 字典列名
     * @return String
     */
    String selectDictCodeByNote(@Param(value = "dictNote")String dictNote,
                                @Param(value = "colName")String colName);
    /**
     * 根据无人机编码查询在线状态
     * @param droneCode 无人机编码
     * @return String
     */
    String selectStatusByDroneCode(@Param(value = "droneCode") String droneCode);
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
     * 根据巡视点查询无人机测点id
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
     * 插入无人机本体告警数据
     * @param tDroneAlarm 无人机本体告警
     * @return int
     */
    int insertDroneAlarm(TDroneAlarm tDroneAlarm);
    /**
     * 插入巡视任务结果数据
     * @param list 无人机巡视点结果集
     * @return int
     */
    int batchInsertCruiseDataResult(List<TCruiseDataResult> list);
    /**
     * 插入巡视任务结果数据
     * @param list 无人机巡视点结果集
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
     * 查询无人机测点配置算法情况
     * @param inspectionCode 无人机测点id
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
    TStdRegion selectTSRegionForStation(@Param(value = "droneId") Long droneId);
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
     * 插入无人机巡检点告警及分析无人机巡检点结果后的告警
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
     * 判断任务是否属于无人机本体任务
     * @param taskId 任务id
     * @return Long
     */
    Long selectIsDroneTask(@Param(value = "taskId")String taskId);
    /**
     * 查询站端的任务在巡视主机上未完成的巡视点
     * @param droneId
     * @return List<String>
     */
    List<String> HasStandTaskIsFinish(@Param(value = "droneId")Long droneId);
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
     * 根据 设备无人机id，查询上级区域
     * @date 2022
     */
    String selectRegionIdBydroneId(@Param(value = "droneCode")String droneCode);

    int updateTCruiseTask(Map<String, Object> taskMap);

    List<String> selectCameraUrlByDeviceId(@Param(value = "deviceId") String deviceId);

    String selectDeviceTypeName(@Param(value = "deviceId") String deviceId);

}
