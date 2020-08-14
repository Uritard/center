package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-13
 */
@Repository
public interface TCameraRecorderDao {

    int insert(TCameraRecorder tCameraRecorder);
    int deleteByPrimaryId(@Param(value = "recordId") Long recordId);
    int update(TCameraRecorder tCameraRecorder);
    //录像机数据主键查询
    TCameraRecorder selectByPrimaryId(@Param(value = "recordId") Long recordId);
    //录像机数据全查询
    List<TCameraRecorder> select(@Param(value = "recordId") Long recordId,
                                 @Param(value = "recordName") String recordName,
                                 @Param(value = "aliasName") String aliasName,
                                 @Param(value = "recordIp") String recordIp,
                                 @Param(value = "protocal") String protocal,
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
    List<TCameraRecorder> selectByPage(TCameraRecorder tCameraRecorder);

//    List<AreaInfo> selectCameraTreeDevice();
}