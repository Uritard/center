package com.yjh.accessvqd.module.diagnose.service;

import com.yjh.accessvqd.commons.logs.Logs;
import com.yjh.accessvqd.module.diagnose.dao.ChanResultDao;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author czh
 * @since 2021-01-14
 */
@Service
public class ChanResultService {
    @Autowired
    private ChanResultDao chanResultDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(ChanResult chanResult) {
        return this.chanResultDao.insert(chanResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long diagnoseResultId) {
        return this.chanResultDao.deleteByPrimaryId(diagnoseResultId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(ChanResult chanResult) {
        return this.chanResultDao.update(chanResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public ChanResult selectByPrimaryId(Long diagnoseResultId) {
        return this.chanResultDao.selectByPrimaryId(diagnoseResultId);
    }

//    @Logs(title = "查询", code = "module")
//    @Transactional(rollbackFor = Exception.class)
//    public List<ChanResult> select(Long diagnoseResultId, String channelId, String ip, String chanIndex, Date checkTime, Integer channelResult, Integer signalResult, Integer blurResult, Integer contrastResult, Integer brightResult, Integer darkResult, Integer chromaResult, Integer monoResult, Integer noiseResult, Integer streakResult, Integer freezeResult, Integer shakeResult, Integer flashResult, Integer sceneResult, Integer coverResult, Integer ptzResult, String snapshotUlt, String resultContent) {
//        List<ChanResult> tDiagnoseResultList = chanResultDao.select(diagnoseResultId, channelId, ip, chanIndex, checkTime, channelResult, signalResult, blurResult, contrastResult, brightResult, darkResult, chromaResult, monoResult, noiseResult, streakResult, freezeResult, shakeResult, flashResult, sceneResult, coverResult, ptzResult, snapshotUlt, resultContent);
//        return tDiagnoseResultList;
//    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<ChanResult> selectByPage(ChanResult chanResult) {
        List<ChanResult> tDiagnoseResultList = chanResultDao.selectByPage(chanResult);
        return tDiagnoseResultList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<ChanResult> list) {
        return this.chanResultDao.batchInsert(list);
    }
}
