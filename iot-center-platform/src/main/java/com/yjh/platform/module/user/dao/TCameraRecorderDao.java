package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yc
 * @since 2020-08-24
 */
@Repository
public interface TCameraRecorderDao {

    int insert(TCameraRecorder tCameraRecorder);
    int deleteByPrimaryId(@Param(value = "recordId") Long recordId);
    int update(TCameraRecorder tCameraRecorder);
    int deleteSelectedRecord(@Param(value = "list") List<String> list);
    TCameraRecorderByDict selectByPrimaryId(@Param(value = "recordId") Long recordId);
    List<TCameraRecorderByDict> select(@Param(value = "recordId") Long recordId,
                                       @Param(value = "recordName") String recordName,
                                       @Param(value = "recorderModel") Integer recorderModel,
                                       @Param(value = "recorderType") String recorderType,
                                       @Param(value = "vendorId")Integer vendorId,
                                       @Param(value = "pmsId") String pmsId,
                                       @Param(value = "aliasName") String aliasName,
                                       @Param(value = "recordIp") String recordIp,
                                       @Param(value = "protocol") String protocol,
                                       @Param(value = "httpPort") Integer httpPort,
                                       @Param(value = "transPort") Integer transPort,
                                       @Param(value = "rtspPort") Integer rtspPort,
                                       @Param(value = "identityManager") String identityManager,
                                       @Param(value = "identityCode") String identityCode,
                                       @Param(value = "protocolUrl") String protocolUrl,
                                       @Param(value = "maxChannel") Integer maxChannel,
                                       @Param(value = "hddSize") Integer hddSize,
                                       @Param(value = "bufferDay") Integer bufferDay,
                                       @Param(value = "timeLong") Integer timeLong,
                                       @Param(value = "unit") String unit);
    List<TCameraRecorderByDict> selectByPage(@Param(value = "recordType") Integer recordType,
                                            @Param(value = "aliasName") String aliasName,
                                             @Param(value = "unit") String unit,
                                             @Param(value = "vendorId") Integer vendorId,
                                             @Param(value = "recorderModel") Integer recorderModel,
                                             @Param(value = "recordName")String recordName);
    int batchInsert(List<TCameraRecorder> list);
    List<TCameraRecorderDetail> selectIdAndName();
    Long selectRecorderIdByPmsId(@Param(value = "pmsId")String pmsId);
    List<Long>selectHaveCamera(@Param(value = "recordId") Long recordId);
    List<String> selectAllPMSId();
    String selectPmsIdById(@Param(value = "recordId")Long recordId);
}
