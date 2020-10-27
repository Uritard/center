package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TDefectInfo;
import com.yjh.platform.module.task.entity.TDefectInfoDetail;
import com.yjh.platform.module.task.entity.WarnStatistical;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-10-15
 */
@Repository
public interface TDefectInfoDao {

    int insert(TDefectInfo tDefectInfo);
    int deleteByPrimaryId(@Param(value = "defectId") Long defectId);
    int update(TDefectInfo tDefectInfo);
    TDefectInfo selectByPrimaryId(@Param(value = "defectId") Long defectId);
    List<TDefectInfo> select(@Param(value = "defectId") Long defectId,
                             @Param(value = "defectLevel") Integer defectLevel,
                             @Param(value = "defectTime") Date defectTime,
                             @Param(value = "defectType") Integer defectType,
                             @Param(value = "defectName") String defectName,
                             @Param(value = "defectContent") String defectContent,
                             @Param(value = "deviceId") Long deviceId,
                             @Param(value = "cunstomId") String cunstomId,
                             @Param(value = "instanceId") Long instanceId,
                             @Param(value = "stdMeteId") Long stdMeteId,
                             @Param(value = "confMode") Integer confMode,
                             @Param(value = "isDefect") Integer isDefect,
                             @Param(value = "dealType") Integer dealType,
                             @Param(value = "dealInfo") String dealInfo,
                             @Param(value = "dealPersonId") String dealPersonId,
                             @Param(value = "dealTime") Date dealTime,
                             @Param(value = "ifDefectDisable") Integer ifDefectDisable,
                             @Param(value = "alarmSource") Integer alarmSource,
                             @Param(value = "defectSubtype") Integer defectSubtype,
                             @Param(value = "deviceCode") String deviceCode,
                             @Param(value = "imagePath") String imagePath,
                             @Param(value = "videoPath") String videoPath,
                             @Param(value = "value") String value,
                             @Param(value = "outRange") String outRange,
                             @Param(value = "linkMessage") String linkMessage);
    List<TDefectInfo> selectByPage(TDefectInfo tDefectInfo);

    int batchInsert(List<TDefectInfo> list);
    List<TDefectInfoDetail> selectDefectProcess(@Param(value = "defectId")Long defectId);
    List<TDefectInfoDetail> selectAllDefect(HashMap<String,Object> map);
    List<WarnStatistical> countDefectOnMonth(@Param(value = "startTime")String startTime,
                                             @Param(value = "endTime")String endTime);

    Map<String, Integer> countDefectConfMode();
}
