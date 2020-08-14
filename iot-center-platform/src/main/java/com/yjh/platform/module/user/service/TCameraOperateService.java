package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.TCameraOperateDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/13 - 13:34
 */
@Service
public class TCameraOperateService {

    @Autowired
    private TCameraOperateDao tCameraOperateDao;

    //摄像机预置位查询
    @Logs(title = "根据id查询摄像机预置位所有信息", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> selectAllByTCPid(Long presetId) {
        return tCameraOperateDao.selectAllByTCPid(presetId);
    }

    //摄像机预置位删除
    @Logs(title = "根据id删除摄像机预置位的信息", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByTCPid(Long presetId) {
        return this.tCameraOperateDao.deleteByTCPid(presetId);
    }
    //摄像机预置位插入
    @Logs(title = "插入摄像机预置位", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public int insertTCP(TCameraPreset tCameraPreset) {

        return this.tCameraOperateDao.insertTCP(tCameraPreset);
    }
    //摄像机预置位更改
    @Logs(title = "更新摄像机预置位", code = "TCameraPreset")
    @Transactional(rollbackFor = Exception.class)
    public int updateTCP(TCameraPreset tCameraPreset) {

        return this.tCameraOperateDao.updateTCP(tCameraPreset);
    }
}
