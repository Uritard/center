package com.yjh.platform.module.task.dao;

import com.yjh.platform.module.task.entity.TDefectInfo;
import com.yjh.platform.module.task.entity.TDefectInfoDetail;
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
    Map<String,Integer> countDefectOnMonth (@Param(value = "firstTime1")String firstTime1,
                                            @Param(value = "firstTime2")String firstTime2,
                                            @Param(value = "firstTime3")String firstTime3,
                                            @Param(value = "firstTime4")String firstTime4,
                                            @Param(value = "firstTime5")String firstTime5,
                                            @Param(value = "firstTime6")String firstTime6,
                                            @Param(value = "firstTime7")String firstTime7,
                                            @Param(value = "firstTime8")String firstTime8,
                                            @Param(value = "firstTime9")String firstTime9,
                                            @Param(value = "firstTime10")String firstTime10,
                                            @Param(value = "firstTime11")String firstTime11,
                                            @Param(value = "firstTime12")String firstTime12,
                                            @Param(value = "firstTime13")String firstTime13,
                                            @Param(value = "firstTime14")String firstTime14,
                                            @Param(value = "firstTime15")String firstTime15,
                                            @Param(value = "firstTime16")String firstTime16,
                                            @Param(value = "firstTime17")String firstTime17,
                                            @Param(value = "firstTime18")String firstTime18,
                                            @Param(value = "firstTime19")String firstTime19,
                                            @Param(value = "firstTime20")String firstTime20,
                                            @Param(value = "firstTime21")String firstTime21,
                                            @Param(value = "firstTime22")String firstTime22,
                                            @Param(value = "firstTime23")String firstTime23,
                                            @Param(value = "firstTime24")String firstTime24,
                                            @Param(value = "firstTime25")String firstTime25,
                                            @Param(value = "firstTime26")String firstTime26,
                                            @Param(value = "firstTime27")String firstTime27,
                                            @Param(value = "firstTime28")String firstTime28,
                                            @Param(value = "firstTime29")String firstTime29,
                                            @Param(value = "firstTime30")String firstTime30,
                                            @Param(value = "firstTime31")String firstTime31);
    Map<String, Integer> countDefectConfMode();
}
