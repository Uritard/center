package com.yjh.platform.module.user.service;

import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.TRecordFileInfoDao;
import com.yjh.platform.module.user.entity.TRecordFileInfo;
import com.yjh.platform.module.video.service.CameraConService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.yjh.platform.common.utils.smUtil.report.FileUtil.deleteFile;

/**
 * @author hyh
 * @since 2022/4/11
 **/
@Slf4j
@Service
public class TRecordFileInfoService {

    @Resource
    private TRecordFileInfoDao tRecordFileInfoDao;

    @Resource
    private CameraConService cameraConService;

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long id) {
        TRecordFileInfo tRecordFileInfo = tRecordFileInfoDao.selectByPrimaryId(id);
        if (StringUtils.isNotEmpty(tRecordFileInfo.getAbsoluteFilePath())){
            deleteFile(tRecordFileInfo.getAbsoluteFilePath());
        }
        return tRecordFileInfoDao.deleteByPrimaryId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRecordFileInfo> selectByPage(Long cameraId, String startTime, String endTime) {
        return tRecordFileInfoDao.selectByPage(cameraId, startTime, endTime);
    }

    public Result getFileList(Long cameraId, String startTime, String endTime) {
        Result result = new Result();
        result.setData(cameraConService.getRecordFiles(cameraId, startTime, endTime));
        return result;
    }
}
