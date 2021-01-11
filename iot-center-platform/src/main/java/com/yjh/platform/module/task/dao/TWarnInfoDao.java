package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TJContentInfoDetail;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
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
public interface TWarnInfoDao {

    int insert(TWarnInfo tWarnInfo);
    int deleteByPrimaryId(@Param(value = "warnId") Long warnId);
    int update(TWarnInfo tWarnInfo);
    TWarnInfo selectByPrimaryId(@Param(value = "warnId") Long warnId);
    List<TWarnInfo> select(@Param(value = "warnId") Long warnId,
                                @Param(value = "warnLevel") Integer warnLevel,
                                @Param(value = "warnTime") Date warnTime,
                                @Param(value = "warnType") Integer warnType,
                                @Param(value = "warnName") String warnName,
                                @Param(value = "warnContent") String warnContent,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "cunstomId") String cunstomId,
                                @Param(value = "instanceId") Long instanceId,
                                @Param(value = "stdMeteId") Long stdMeteId,
                                @Param(value = "confMode") Integer confMode,
                                @Param(value = "isWarn") Integer isWarn,
                                @Param(value = "dealType") Integer dealType,
                                @Param(value = "dealInfo") String dealInfo,
                                @Param(value = "dealPersonId") String dealPersonId,
                                @Param(value = "dealTime") Date dealTime,
                                @Param(value = "defectModel") Integer defectModel,
                                @Param(value = "alarmSource") Integer alarmSource,
                                @Param(value = "warnSubtype") Integer warnSubtype,
                                @Param(value = "deviceCode") String deviceCode,
                                @Param(value = "imagePath") String imagePath,
                                @Param(value = "videoPath") String videoPath,
                                @Param(value = "value") String value,
                                @Param(value = "outRange") String outRange,
                                @Param(value = "taskId") String taskId);
    List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo);

    int batchInsert(List<TWarnInfo> list);
    List<TWarnInfoDetail> selectAllWarn(HashMap<String,Object> map);
    List<TWarnInfoDetail> WarnConfirm(HashMap<String,Object> map);
    Map<String, Integer> countByAlarmSource();
    List<TJContentInfoDetail>  countByDeviceType(@Param(value = "startTime")String startTime,
                                                 @Param(value = "endTime")String endTime);
    Map<String, Integer> countWarnConfMode(@Param(value = "startTime")String startTime,
                                           @Param(value = "endTime")String endTime);
    List<TWarnInfoDetail> selectAlarmProcess(@Param(value = "warnId")Long warnId);
    List<WarnStatistical> countWarnOnMonth();
    //查询未审核告警信息数量(查询所有未被核查的告警信息条数)
    Integer warnCountsNonIdentify();

    TWarnInfoDetail selectWarnPopUp(@Param(value = "warnId")Long warnId);
}
