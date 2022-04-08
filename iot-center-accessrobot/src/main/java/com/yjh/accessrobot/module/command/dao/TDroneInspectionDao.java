package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TDroneInspection;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TDroneInspectionDao {

    int insert(TDroneInspection tDroneInspection);
    int deleteByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    int update(TDroneInspection tDroneInspection);
    TDroneInspection selectByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    List<TDroneInspection> select(@Param(value = "inspectionId") Long inspectionId,
                                  @Param(value = "inspectionCode")String inspectionCode,
                                  @Param(value = "droneId") Long droneId,
                                  @Param(value = "inspectionName") String inspectionName,
                                  @Param(value = "inspectionType") Integer inspectionType,
                                  @Param(value = "componentId") String componentId,
                                  @Param(value = "meterType") Integer meterType,
                                  @Param(value = "appearanceType") Integer appearanceType,
                                  @Param(value = "mainOperationType") Integer mainOperationType,
                                  @Param(value = "operationType") Integer operationType,
                                  @Param(value = "saveTypeList") String saveTypeList,
                                  @Param(value = "recognitionTypeList") String recognitionTypeList,
                                  @Param(value = "phase") String phase,
                                  @Param(value = "deviceInfo") String deviceInfo);
    List<TDroneInspection> selectByPage(TDroneInspection tDroneInspection);

    int batchInsertTDroneInspection(List<TDroneInspection> list);
    List<String> selectAllByDroneId(@Param(value = "droneId") Long droneId);

    List<Long> selectInspectionIdList(List<String> list);
    List<Long> selectInstanceIdList(List<Long> list);
    int batchDeleteTDroneInspection(List<Long> list);
    int batchDeleteTCruisePointInstance(List<Long> list);
    int batchDeleteTCruisePlanAttr(List<Long> list);
    Map<String,Object> selectInspection(@Param(value = "inspectionCode") String inspectionCode);
}
