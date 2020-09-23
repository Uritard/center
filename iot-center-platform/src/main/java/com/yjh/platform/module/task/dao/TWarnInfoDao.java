package com.yjh.platform.module.task.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.RobotAlarm;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-24
 */
@Repository
public interface TWarnInfoDao {

    int insert(TWarnInfo tWarnInfo);
    int deleteByPrimaryId(@Param(value = "warnId") Long warnId);
    int update(TWarnInfo tWarnInfo);
    TWarnInfo selectByPrimaryId(@Param(value = "warnId") Long warnId);
    List<TWarnInfo> select(@Param(value = "warnId") Long warnId,
                                @Param(value = "warnLevel") Integer warnLevel,
                                @Param(value = "warnTime") Date warnTime,
                                @Param(value = "warnType") Integer warnType,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "cunstomId") String cunstomId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "stdMeteId") Long stdMeteId,
                                @Param(value = "confMode") Integer confMode,
                                @Param(value = "confTime") Date confTime,
                                @Param(value = "confUserId") String confUserId,
                                @Param(value = "confInfo") String confInfo,
                                @Param(value = "ifWarnDisable") Integer ifWarnDisable,
                                @Param(value = "alarmSource") Integer alarmSource,
                                @Param(value = "warnSubtype") Integer warnSubtype,
                                @Param(value = "deviceCode") String deviceCode,
                                @Param(value = "imagePath") String imagePath,
                                @Param(value = "videoPath") String videoPath,
                                @Param(value = "value") String value,
                                @Param(value = "defect") Integer defect,
                                @Param(value = "defectLevel") Integer defectLevel,
                                @Param(value = "outRange") String outRange,
                                @Param(value = "linkMessage") String linkMessage);
    List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo);

    int batchInsert(List<TWarnInfo> list);
    List<TWarnInfoDetail> selectAllWarn(HashMap<String,Object> map);
    List<HashMap<String,Integer>> countOnMonth(HashMap<String,Object> map);


    List<HashMap<String,Object>> selectByType(HashMap<String,Object> map);
    List<RobotAlarm> selectByRobot(HashMap<String,Object> map);
    List<HashMap<String,Object>> selectOthers(HashMap<String,Object> map);
    List<HashMap<String,Integer>> countByType(HashMap<String,Object> map);
    List<HashMap<String,Integer>> countOthers(HashMap<String,Object> map);
    List<HashMap<String,Integer>> countByRobot(HashMap<String,Object> map);


}
