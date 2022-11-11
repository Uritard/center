package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TRobotInspection;
import org.apache.commons.collections4.SetUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @since 2020-11-19
 */
@Repository
public interface TRobotInspectionDao {

    int insert(TRobotInspection tRobotInspection);
    int deleteByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    int update(TRobotInspection tRobotInspection);
    TRobotInspection selectByPrimaryId(@Param(value = "inspectionId") Long inspectionId);
    List<TRobotInspection> select(@Param(value = "inspectionId") Long inspectionId,
                                  @Param(value = "inspectionCode")String inspectionCode,
                                  @Param(value = "robotId") Long robotId,
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
    List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection);

    int batchInsertTRobotInspection(List<TRobotInspection> list);
    List<String> selectAllByRobotId(@Param(value = "robotId") Long robotId);

    List<Long> selectInspectionIdList(List<String> list);
    List<Long> selectInstanceIdList(List<Long> list);
    int batchDeleteTRobotInspection(List<Long> list);
    int batchDeleteTCruisePointInstance(List<Long> list);
    int batchDeleteTCruisePlanAttr(List<Long> list);
    Map<String,Object> selectInspection(@Param(value = "inspectionCode") String inspectionCode);

    List<TRobotInspection> selectByEdgeCode(@Param("edgeCode") String edgeCode);

    int insertSelective(TRobotInspection record);

    int updateByPrimaryKeySelective(TRobotInspection record);

    int updateByPrimaryKey(TRobotInspection record);

    int deleteByEdgeCodeAndOriginId(@Param("edgeCode") String edgeCode , @Param("originIdList") Collection<String> originIdList );
}
