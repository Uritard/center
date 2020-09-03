package com.yjh.platform.module.device.dao;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.device.entity.TStdRegion;
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
                                      @Param(value = "unitVal") String unitVal,
                                      @Param(value = "unitName") String unitName,
                                      @Param(value = "ifSy") Integer ifSy,
                                      @Param(value = "syType") Integer syType,
                                      @Param(value = "ifVideotape") Integer ifVideotape,
                                      @Param(value = "videotapeTime") String videotapeTime,
                                      @Param(value = "textDesc") String textDesc,
                                      @Param(value = "sort") String sort);
    List<TCruisePointInstanceDetail> selectByPage(TCruisePointInstanceDetail tCruisePointInstanceDetail);

    int batchInsert(List<TCruisePointInstance> list);
    List<TCruisePointInstance> StdMeteUnionInspectionId(@Param(value = "deviceId") Long deviceId);

    //为创建实例服务
    TStdRegion selectTSRegionForStation();
    TDictBusiness selectTDBusinessForUnitName(String colName);
    //统计当前任务下的巡检点数量
    int selectCruiseCount(@Param(value = "taskId")Long taskId);

    //根据巡检点类型查询巡检点数量
    int selectCruiseCountByType(@Param(value = "taskId")Long taskId,
            @Param(value = "cruiseType")Integer cruiseType);
}