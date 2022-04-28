package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Repository
public interface TCruiseNonhomologousPointInstanceDao {

    int insert(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance);
    int deleteByPrimaryId(@Param(value = "instanceId") Long instanceId);
    int update(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance);
    TCruisePointInstance selectByPrimaryId(@Param(value = "instanceId") Long instanceId);
    Map<String,Object> selectWarnByPrimaryId(@Param(value = "warnId") String warnId);
    List<Map<String,Object>> selectWarnInspections(@Param(value = "warnId") String warnId);
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
    List<TCruiseNonhomologousPointInstance> selectByPage(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance);

    List<TCruiseNonhomologousWarnInfo> selectWarnByPage(TCruiseNonhomologousWarnInfo tCruiseNonhomologousWarnInfo);

    int delete(TCruisePointInstance tCruisePointInstance);

    int batchInsert(List<TCruisePointInstance> list);

    int deleteByInstanceId(@Param(value = "list") List<Long> list);

    int checkNonhomologousPointInstanceExist(TCruiseNonhomologousPointInstance tCruiseNonhomologousPointInstance);
}