package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-08
 */
@Repository
public interface TCruisePointInstanceDao {

    int insert(TCruisePointInstance tCruisePointInstance);
    int deleteByPrimaryId(@Param(value = "instanceId") Long instanceId);
    int deleteByDeviceMeteId(@Param(value = "deviceMeteId") Long deviceMeteId);
    int deleteByDeviceId(@Param(value = "deviceId") Long deviceId);
    int update(TCruisePointInstance tCruisePointInstance);
    TCruisePointInstance selectByPrimaryId(@Param(value = "instanceId") Long instanceId);
    List<TCruisePointInstance> select(@Param(value = "instanceId") Long instanceId,
                                      @Param(value = "deviceMeteId") Long deviceMeteId,
                                      @Param(value = "stationId") String stationId,
                                      @Param(value = "stationName") String stationName,
                                      @Param(value = "deviceId") Long deviceId,
                                      @Param(value = "customId") String customId,
                                      @Param(value = "dataFormat") String dataFormat,
                                      @Param(value = "identifyType") Integer identifyType,
                                      @Param(value = "identifySonType") Integer identifySonType,
                                      @Param(value = "cruiseType") Integer cruiseType,
                                      @Param(value = "cruiseId") Long cruiseId,
                                      @Param(value = "cruiseName") String cruiseName,
                                      @Param(value = "cruiseContent") String cruiseContent,
                                      @Param(value = "positionType") String positionType,
                                      @Param(value = "unit") String unit,
                                      @Param(value = "ifSy") Integer ifSy,
                                      @Param(value = "syType") Integer syType,
                                      @Param(value = "ifVideotape") Integer ifVideotape,
                                      @Param(value = "videotapeTime") String videotapeTime,
                                      @Param(value = "textDesc") String textDesc,
                                      @Param(value = "sort") String sort);
    List<TCruisePointInstance> selectByPage(TCruisePointInstance tCruisePointInstance);

    int delete(TCruisePointInstance tCruisePointInstance);

    List<Long> selectForCruiseByPage(TStdDeviceMete tStdDeviceMete);
    List<TStdDeviceMeteForPointDetail> selectCruisePointByPage(@Param(value = "list") List<Long> list);

    List<String> selectSYForCruiseByPage(TCfgMeteForPointDetail tCfgMeteForPointDetail);
    List<TCfgMeteForPointDetail> selectSYCruisePointByPage(@Param(value = "list") List<String> list);

    int batchInsert(List<TCruisePointInstance> list);
    List<TCruisePointInstance> StdMeteUnionInspectionId(@Param(value = "deviceId") Long deviceId);

    //为创建实例服务
    TStdRegion selectTSRegionForStation();
    TDictBusiness selectTDBusinessForUnitName(String dictCode);
    //统计当前任务下的巡检点数量
    int selectCruiseCount(@Param(value = "taskId")String taskId);

    //根据巡检点类型查询巡检点数量
    List<CruiseCountOfType> selectCruiseCountByType(@Param(value = "taskId")String taskId);
    String selectInstancename(@Param("instanceId")Long instanceId);

    //查询巡检类型
    CruiseTypeInfo selectCruiseCommonInfoByInstanceId(@Param(value = "instanceId")Long instanceId);
    List<Long> selectCruiseId(TCruisePointInstanceDetail tCruisePointInstanceDetail);
    List<Long> selectInstanceId(@Param(value = "ids")List<Long> ids,
                          @Param(value = "deviceMeteId") Long deviceMeteId);
    List<TCruisePointInstance> selectForTask(@Param(value = "list")List<Long> list);

    int deleteByInstanceId(@Param(value = "list")List<Long> list);
}