package com.yjh.accessvideo.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.accessvideo.module.device.entity.TCruiseDataResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-10-20
 */
@Repository
public interface TCruiseDataResultDao {

    int insert(TCruiseDataResult tCruiseDataResult);
    int deleteByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    int update(TCruiseDataResult tCruiseDataResult);
    TCruiseDataResult selectByPrimaryId(@Param(value = "cruiseDataId") Long cruiseDataId);
    List<TCruiseDataResult> select(@Param(value = "cruiseDataId") Long cruiseDataId,
                                @Param(value = "cruiseResultId") String cruiseResultId,
                                @Param(value = "cruiseId") Long cruiseId,
                                @Param(value = "cruiseType") Integer cruiseType,
                                @Param(value = "resultDesc") String resultDesc,
                                @Param(value = "resultNum") String resultNum,
                                @Param(value = "modifyNum") String modifyNum,
                                @Param(value = "picpath") String picpath,
                                @Param(value = "personCheck") String personCheck,
                                @Param(value = "origpic") String origpic,
                                @Param(value = "state") Integer state,
                                @Param(value = "evaluationState") Integer evaluationState,
                                @Param(value = "identifyState") Integer identifyState,
                                @Param(value = "identifyResult") Integer identifyResult,
                                @Param(value = "createtime") Date createtime,
                                @Param(value = "remark") String remark,
                                @Param(value = "checkUser") String checkUser,
                                @Param(value = "checkDate") Date checkDate);
    List<TCruiseDataResult> selectByPage(TCruiseDataResult tCruiseDataResult);

    int batchInsert(List<TCruiseDataResult> list);
}
