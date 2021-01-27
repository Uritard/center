package com.yjh.accessvqd.module.diagnose.service;

import com.yjh.accessvqd.commons.logs.Logs;
import com.yjh.accessvqd.module.diagnose.dao.ChanResultDao;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.entity.DiagnoseResultDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author czh
 * @since 2021-01-14
 */
@Service
public class ChanResultService {
    @Autowired
    private ChanResultDao chanResultDao;

    Logger log = LoggerFactory.getLogger(ChanResultService.class);

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

    @Logs(title = "分页查询诊断结果详细信息", code = "chanResult")
    @Transactional(rollbackFor = Exception.class)
    public List<DiagnoseResultDetail> selectDiagnoseResultByPage(String planName, String channelName, Date startTime, Date endTime, String diagnosePlanId) {
        List<DiagnoseResultDetail> details = chanResultDao.selectDiagnoseResultByPage(planName, channelName, startTime, endTime, diagnosePlanId);
        log.info("result：" + details);
        for (DiagnoseResultDetail detail : details) {
            detail.setResolving(detail.getWidth() + "*" + detail.getHeight());
            checkItemsOperate(detail);
            switch (detail.getDevType()){
                case "0":
                    detail.setDevType("枪机");
                    break;
                case "1":
                    detail.setDevType("球机");
                    break;
            }
        }
        return details;
    }


    public DiagnoseResultDetail checkItemsOperate(DiagnoseResultDetail diagnoseResultDetail) {
        // TODO: 2021/1/25  1.诊断状态判定  2.诊断结果汇总
        String content = "";//诊断结果汇总
        if (diagnoseResultDetail.getChannelResult() > 1) {
            diagnoseResultDetail.setStatus("故障");
            switch (diagnoseResultDetail.getChannelResult()) {
                case 2:
                    content = "异常";
                    break;
                case 3:
                    content = "登录失败";
                    break;
                case 4:
                    content = "取流异常";
                    break;
                case 5:
                    content = "解码失败";
                    break;
                case 6:
                    content = "取流延时";
                    break;

            }
        } else {
            if (diagnoseResultDetail.getSignalResult().equals("2")) {
                content = "信号丢失";
            }
            if (diagnoseResultDetail.getBlurResult().equals("2")) {
                content = content + "图像模糊";
            }

            if (diagnoseResultDetail.getContrastResult().equals("2")) {
                content = content + "对比度";
            }

            if (diagnoseResultDetail.getBrightResult().equals("2")) {
                content = content + "图像过亮";
            }

            if (diagnoseResultDetail.getDarkResult().equals("2")) {
                content = content + "图像过暗";
            }

            if (diagnoseResultDetail.getChromaResult().equals("2")) {
                content = content + "图像偏色";
            }

            if (diagnoseResultDetail.getMonoResult().equals("2")) {
                content = content + "黑白图像";
            }

            if (diagnoseResultDetail.getNoiseResult().equals("2")) {
                content = content + "噪声干扰";
            }

            if (diagnoseResultDetail.getStreakResult().equals("2")) {
                content = content + "条纹干扰";
            }

            if (diagnoseResultDetail.getFreezeResult().equals("2")) {
                content = content + "画面冻结";
            }

            if (diagnoseResultDetail.getShakeResult().equals("2")) {
                content = content + "视频抖动";
            }

            if (diagnoseResultDetail.getFlashResult().equals("2")) {
                content = content + "视频剧变";
            }

            if (diagnoseResultDetail.getSceneResult().equals("2")) {
                content = content + "场景变换";
            }

            if (diagnoseResultDetail.getCoverResult().equals("2")) {
                content = content + "视频遮挡";
            }

            if (diagnoseResultDetail.getPtzResult().equals("2")) {
                content = content + "云台失控";
            }


            if (content.equals("")) {
                diagnoseResultDetail.setStatus("正常");
            } else {
                diagnoseResultDetail.setStatus("故障");
            }

        }

        //检测项赋值
        diagnoseResultDetail.setSignalResult(checkResult(diagnoseResultDetail.getSignalResult()));
        diagnoseResultDetail.setBlurResult(checkResult(diagnoseResultDetail.getBlurResult()));
        diagnoseResultDetail.setContrastResult(checkResult(diagnoseResultDetail.getContrastResult()));
        diagnoseResultDetail.setBrightResult(checkResult(diagnoseResultDetail.getBrightResult()));
        diagnoseResultDetail.setDarkResult(checkResult(diagnoseResultDetail.getDarkResult()));
        diagnoseResultDetail.setChromaResult(checkResult(diagnoseResultDetail.getChromaResult()));
        diagnoseResultDetail.setMonoResult(checkResult(diagnoseResultDetail.getMonoResult()));
        diagnoseResultDetail.setNoiseResult(checkResult(diagnoseResultDetail.getNoiseResult()));
        diagnoseResultDetail.setStreakResult(checkResult(diagnoseResultDetail.getStreakResult()));
        diagnoseResultDetail.setFreezeResult(checkResult(diagnoseResultDetail.getFreezeResult()));
        diagnoseResultDetail.setShakeResult(checkResult(diagnoseResultDetail.getShakeResult()));
        diagnoseResultDetail.setFlashResult(checkResult(diagnoseResultDetail.getFlashResult()));
        diagnoseResultDetail.setSceneResult(checkResult(diagnoseResultDetail.getSceneResult()));
        diagnoseResultDetail.setCoverResult(checkResult(diagnoseResultDetail.getCoverResult()));
        diagnoseResultDetail.setPtzResult(checkResult(diagnoseResultDetail.getPtzResult()));

        diagnoseResultDetail.setResultContent(content);


        return diagnoseResultDetail;
    }

    public String checkResult(String point) {
        String checkItems = "";
        switch (point) {
            case "0":
                checkItems = "未检测";
                break;
            case "1":
                checkItems = "检测正常";
                break;
            case "2":
                checkItems = "检测异常";
                break;
            case "3":
                checkItems = "登录失败";
                break;
            case "4":
                checkItems = "取流失败";
                break;
            case "5":
                checkItems = "解码失败";
                break;
            case "6":
                checkItems = "取流延时";
                break;
        }
        return checkItems;
    }


    @Logs(title = "统计不同故障类型的诊断点数")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Long> faultCounts(String diagnosePlanId) {
        return chanResultDao.faultTypeSta(diagnosePlanId);
    }

    @Logs(title = "统计不同监测点状态")
    public Map<String, Long> statusTypeChannel(String diagnosePlanId) {
        return chanResultDao.statusTypeChannel(diagnosePlanId);
    }

    @Logs(title = "故障信息树")
    public Map<String, Integer> faultTree() {
        Map<String, Integer> faultItems = new HashMap<>();
        faultItems.put("sta.signal",1 );
        faultItems.put("sta.blur", 1);
        faultItems.put("sta.contrast", 1);
        faultItems.put("sta.bright", 1);
        faultItems.put("sta.dark", 1);
        faultItems.put("sta.chroma", 1);
        faultItems.put("sta.mono", 1);
        faultItems.put("sta.noise", 1);
        faultItems.put("sta.streak", 1);
        faultItems.put("sta.freeze", 1);
        faultItems.put("sta.shake", 1);
        faultItems.put("sta.flash", 1);
        faultItems.put("sta.scene", 1);
        faultItems.put("sta.cover",1 );
        faultItems.put("sta.ptz", 1);
        faultItems.put("sta.login", 1);
        faultItems.put("sta.stream", 1);
        return faultItems;
    }

    @Logs(title = "统计分析")
    public Map<String,List<Map<String, Object>>> staticalAnalysis(Map<String,Object> faultInfo) throws ParseException {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime=null;
        Date endTime=null;
        if(Objects.nonNull(faultInfo.get("startTime"))){
             startTime=simpleDateFormat.parse(faultInfo.get("startTime").toString());
             endTime=simpleDateFormat.parse(faultInfo.get("endTime").toString());
            faultInfo.remove("startTime");
            faultInfo.remove("endTime");
        }
        List<Map<String,Object>> charData=new ArrayList<>();
        List<Map<String,Object>> gridData=new ArrayList<>();
        List<Map<String,Object>> mapList=chanResultDao.staticalAnalysis(startTime,endTime);
        Set<String> faultItems=faultInfo.keySet();
        for(String item:faultItems){
            switch (item){
                case "sta.signal":
                    charData.add(mapList.get(0));
                    gridData.add(mapList.get(0));
                break;
                case "sta.blur":
                    charData.add(mapList.get(1));
                    gridData.add(mapList.get(1));
                    break;
                case "sta.contrast" :
                    charData.add(mapList.get(2));
                    gridData.add(mapList.get(2));
                    break;
                case "sta.bright":
                    charData.add(mapList.get(3));
                    gridData.add(mapList.get(3));
                    break;
                case "sta.dark":
                    charData.add(mapList.get(4));
                    gridData.add(mapList.get(4));
                    break;
                case "sta.chroma":
                    charData.add(mapList.get(5));
                    gridData.add(mapList.get(5));
                    break;
                case "sta.mono" :
                    charData.add(mapList.get(6));
                    gridData.add(mapList.get(6));
                    break;
                case "sta.noise":
                    charData.add(mapList.get(7));
                    gridData.add(mapList.get(7));
                    break;
                case "sta.streak":
                    charData.add(mapList.get(8));
                    gridData.add(mapList.get(8));
                    break;
                case "sta.freeze":
                    charData.add(mapList.get(9));
                    gridData.add(mapList.get(9));
                    break;
                case "sta.shake":
                    charData.add(mapList.get(10));
                    gridData.add(mapList.get(10));
                    break;
                case "sta.flash":
                    charData.add(mapList.get(11));
                    gridData.add(mapList.get(11));
                    break;
                case "sta.scene":
                    charData.add(mapList.get(12));
                    gridData.add(mapList.get(12));
                    break;
                case "sta.cover":
                    charData.add(mapList.get(13));
                    gridData.add(mapList.get(13));
                    break;
                case "sta.ptz":
                    charData.add(mapList.get(14));
                    gridData.add(mapList.get(14));
                    break;
                case "sta.login":
                    charData.add(mapList.get(15));
                    gridData.add(mapList.get(15));
                    break;
                case "sta.stream":
                    charData.add(mapList.get(16));
                    gridData.add(mapList.get(16));
                    break;
            }
        }

        Map<String,List<Map<String, Object>>> map=new HashMap<>();
        map.put("charData",charData);
        map.put("gridData",gridData);
        return map;
    }
}
