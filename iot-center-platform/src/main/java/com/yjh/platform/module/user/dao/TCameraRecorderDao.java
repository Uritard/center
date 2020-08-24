package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
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
    int deleteSelectedRecord(@Param(value = "recordIds") String[] recordIds);
    TCameraRecorderByDict selectByPrimaryId(@Param(value = "recordId") Long recordId);
    List<TCameraRecorderByDict> select(@Param(value = "recordId") Long recordId,
                                       @Param(value = "recordName") String recordName,
                                       @Param(value = "recorderType") String recorderType,
                                       @Param(value = "aliasName") String aliasName,
                                       @Param(value = "recordIp") String recordIp,
                                       @Param(value = "protocol") String protocol,
                                       @Param(value = "httpPort") Integer httpPort,
                                       @Param(value = "transPort") Integer transPort,
                                       @Param(value = "rtspPort") Integer rtspPort,
                                       @Param(value = "userName") String userName,
                                       @Param(value = "pwd") String pwd,
                                       @Param(value = "root") String root,
                                       @Param(value = "maxChannel") Integer maxChannel,
                                       @Param(value = "hddSize") Integer hddSize,
                                       @Param(value = "bufferDay") Integer bufferDay,
                                       @Param(value = "timeLong") Integer timeLong);
    List<TCameraRecorderByDict> selectByPage(TCameraRecorder tCameraRecorder);

    int batchInsert(List<TCameraRecorder> list);
}
