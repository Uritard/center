package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.TCruisePointInstance;
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
                                @Param(value = "fluctuatingValue") String fluctuatingValue,
                                @Param(value = "unitVal") String unitVal,
                                @Param(value = "unitName") String unitName,
                                @Param(value = "ifSy") Integer ifSy,
                                @Param(value = "syType") Integer syType,
                                @Param(value = "ifVideotape") Integer ifVideotape,
                                @Param(value = "videotapeTime") String videotapeTime,
                                @Param(value = "textDesc") String textDesc,
                                @Param(value = "sort") String sort);
    List<TCruisePointInstance> selectByPage(TCruisePointInstance tCruisePointInstance);

    int batchInsert(List<TCruisePointInstance> list);
    List<TCruisePointInstance> StdMeteUnionInspectionId(@Param(value = "deviceId") Long deviceId);
}
