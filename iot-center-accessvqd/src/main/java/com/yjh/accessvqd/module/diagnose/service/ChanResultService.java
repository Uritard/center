package com.yjh.accessvqd.module.diagnose.service;

import com.yjh.accessvqd.common.Constant;
import com.yjh.accessvqd.commons.logs.Logs;
import com.yjh.accessvqd.commons.logs.SpringBeanUtils;
import com.yjh.accessvqd.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.module.diagnose.dao.ChanResultDao;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.entity.DiagnoseResultDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${diagnose.picStore}")
    private String VQD_IMAGES_STORE_URL;

    Logger log = LoggerFactory.getLogger(ChanResultService.class);

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(ChanResult chanResult) {
        if (!(chanResult.getSnapshotUrl().equals("(null)"))) {
            chanResult.setSnapshotUrl(chanResult.getSnapshotUrl().replaceAll("192.168.10.34:81",VQD_IMAGES_STORE_URL));
        }
        switch (chanResult.getChannelResult()) {
            case 0:
                chanResult.setStatus("未检测");
                chanResult.setResultContent("未检测");
                break;
            case 1:
                chanResult.setStatus("正常");
                chanResult.setResultContent("正常");
                break;
            case 2:
                chanResult.setStatus("故障");
                checkItems(chanResult);
                break;
            case 3:
                chanResult.setStatus("故障");
                chanResult.setResultContent("登录异常");
                break;
            case 4:
                chanResult.setStatus("故障");
                chanResult.setResultContent("取流异常");
                break;
            case 5:
                chanResult.setStatus("故障");
                chanResult.setResultContent("解码失败");
                break;
            case 6:
                chanResult.setStatus("故障");
                chanResult.setResultContent("取流延时");
                break;

        }


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


    @Logs(title = "查询诊断任务或监测点信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, String>> findQueryItems(Integer queryType) {
        List<Map<String, String>> queryItems = new ArrayList<>();
        queryItems = chanResultDao.selectQueryItems(queryType);
        return queryItems;
    }

    @Logs(title = "分页查询诊断结果详细信息", code = "chanResult")
    @Transactional(rollbackFor = Exception.class)
    public List<DiagnoseResultDetail> selectDiagnoseResultByPage(String pointId, Date startTime, Date endTime, String diagnosePlanId, String status) throws Exception {

        List<DiagnoseResultDetail> details=new ArrayList<>();

        log.info("details:---------" + details);
        log.info("result：" + details);
        //无任务时返回[]结果信息
        if(Objects.isNull(diagnosePlanId) || diagnosePlanId.equals("")){
           return details;
        }else {
            details = chanResultDao.selectDiagnoseResultByPage(pointId, startTime, endTime, diagnosePlanId, status);
            for (DiagnoseResultDetail detail : details) {
                detail.setResolving(detail.getWidth() + "*" + detail.getHeight());
                checkItemsOperate(detail);
                //获取视频地址
                Result result = Constant.otherServer(detail.getCameraId(), Constant.START_CAMERA_URL);
                Map<String, String> videoInfo = (Map<String, String>) result.getData();
                detail.setRtmpUrl(videoInfo.get("rtmpUrl"));
                detail.setFlvUrl(videoInfo.get("flvUrl"));
                switch (detail.getDevType()) {
                    case "0":
                        detail.setDevType("枪机");
                        break;
                    case "1":
                        detail.setDevType("球机");
                        break;
                }
            }
        }
        return details;
    }


    public ChanResult checkItems(ChanResult chanResult) {
        String content = "";

        if (chanResult.getSignalResult().equals("2")) {
            content = "信号丢失 ";
        }
        if (chanResult.getBlurResult().equals("2")) {
            content = content + "图像模糊 ";
        }

        if (chanResult.getContrastResult().equals("2")) {
            content = content + "对比度 ";
        }

        if (chanResult.getBrightResult().equals("2")) {
            content = content + "图像过亮 ";
        }

        if (chanResult.getDarkResult().equals("2")) {
            content = content + "图像过暗 ";
        }

        if (chanResult.getChromaResult().equals("2")) {
            content = content + "图像偏色 ";
        }

        if (chanResult.getMonoResult().equals("2")) {
            content = content + "黑白图像 ";
        }

        if (chanResult.getNoiseResult().equals("2")) {
            content = content + "噪声干扰 ";
        }

        if (chanResult.getStreakResult().equals("2")) {
            content = content + "条纹干扰 ";
        }

        if (chanResult.getFreezeResult().equals("2")) {
            content = content + "画面冻结 ";
        }

        if (chanResult.getShakeResult().equals("2")) {
            content = content + "视频抖动 ";
        }

        if (chanResult.getFlashResult().equals("2")) {
            content = content + "视频剧变 ";
        }

        if (chanResult.getSceneResult().equals("2")) {
            content = content + "场景变换 ";
        }

        if (chanResult.getCoverResult().equals("2")) {
            content = content + "视频遮挡 ";
        }

        if (chanResult.getPtzResult().equals("2")) {
            content = content + "云台失控 ";
        }

        String temContent = content.replaceAll("\\s+", ",");
        chanResult.setResultContent(temContent.substring(0, temContent.length() - 1));
        return chanResult;
    }

    public DiagnoseResultDetail checkItemsOperate(DiagnoseResultDetail diagnoseResultDetail) {
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
    public Map<String, Long> faultCounts(String planId, String channelId) {
        return chanResultDao.faultTypeSta(planId, channelId);
    }

    @Logs(title = "统计不同监测点状态")
    public Map<String, Long> statusTypeChannel(String planId, String channelId) {
        return chanResultDao.statusTypeChannel(planId, channelId);
    }

    @Logs(title = "故障信息树")
    public List<Map<String, String>> faultTree() {
        List<Map<String, String>> faultItems = new ArrayList<>();
        String[] keys = {"signal", "blur", "contrast", "bright", "dark", "chroma", "mono", "noise", "streak", "freeze", "shake", "flash", "scene", "cover", "ptz", "login", "stream"};
        String[] names = {"信号丢失", "图像模糊", "对比度", "图像过亮", "图像过暗", "图像偏色", "黑白图像", "噪声干扰", "条纹干扰", "画面冻结", "视频抖动", "视频剧变", "场景变换", "视频遮挡", "云台失控", "登录失败", "取流异常"};
        for (int i = 0; i < keys.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("checked", "1");
            item.put("key", keys[i]);
            item.put("name", names[i]);
            faultItems.add(item);
        }
        return faultItems;
    }

    @Logs(title = "统计分析")
    public Map<String, List<Map<String, Object>>> staticalAnalysis(String faultInfo, String startDate, String endDate) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        Date endTime = null;
        if (Objects.nonNull(startDate)) {
            if (!(startDate.equals(""))) {
                startTime = simpleDateFormat.parse(startDate);
            }

        }
        if (Objects.nonNull(endDate)) {
            if (!(endDate.equals(""))) {
                endTime = simpleDateFormat.parse(endDate);
            }

        }
        List<Map<String, Object>> charData = new ArrayList<>();
        List<Map<String, Object>> gridData = new ArrayList<>();
        List<Map<String, Object>> mapList = chanResultDao.staticalAnalysis(startTime, endTime);

        String[] faultArr = faultInfo.split(",");
        for (int i = 0; i < faultArr.length; i++) {
            switch (faultArr[i]) {
                case "signal":
                    charData.add(mapList.get(0));
                    gridData.add(mapList.get(0));
                    break;
                case "blur":
                    charData.add(mapList.get(1));
                    gridData.add(mapList.get(1));
                    break;
                case "contrast":
                    charData.add(mapList.get(2));
                    gridData.add(mapList.get(2));
                    break;
                case "bright":
                    charData.add(mapList.get(3));
                    gridData.add(mapList.get(3));
                    break;
                case "dark":
                    charData.add(mapList.get(4));
                    gridData.add(mapList.get(4));
                    break;
                case "chroma":
                    charData.add(mapList.get(5));
                    gridData.add(mapList.get(5));
                    break;
                case "mono":
                    charData.add(mapList.get(6));
                    gridData.add(mapList.get(6));
                    break;
                case "noise":
                    charData.add(mapList.get(7));
                    gridData.add(mapList.get(7));
                    break;
                case "streak":
                    charData.add(mapList.get(8));
                    gridData.add(mapList.get(8));
                    break;
                case "freeze":
                    charData.add(mapList.get(9));
                    gridData.add(mapList.get(9));
                    break;
                case "shake":
                    charData.add(mapList.get(10));
                    gridData.add(mapList.get(10));
                    break;
                case "flash":
                    charData.add(mapList.get(11));
                    gridData.add(mapList.get(11));
                    break;
                case "scene":
                    charData.add(mapList.get(12));
                    gridData.add(mapList.get(12));
                    break;
                case "cover":
                    charData.add(mapList.get(13));
                    gridData.add(mapList.get(13));
                    break;
                case "ptz":
                    charData.add(mapList.get(14));
                    gridData.add(mapList.get(14));
                    break;
                case "login":
                    charData.add(mapList.get(15));
                    gridData.add(mapList.get(15));
                    break;
                case "stream":
                    charData.add(mapList.get(16));
                    gridData.add(mapList.get(16));
                    break;
            }
        }

//        if(faultInfo.get(0).get("checked").equals("1")){
//            charData.add(mapList.get(0));
//            gridData.add(mapList.get(0));
//        }
//        if(faultInfo.get(1).get("checked").equals("1")){
//            charData.add(mapList.get(1));
//            gridData.add(mapList.get(1));
//        }
//        if(faultInfo.get(2).get("checked").equals("1")){
//            charData.add(mapList.get(2));
//            gridData.add(mapList.get(2));
//        }
//        if(faultInfo.get(3).get("checked").equals("1")){
//            charData.add(mapList.get(3));
//            gridData.add(mapList.get(3));
//        }
//        if(faultInfo.get(4).get("checked").equals("1")){
//            charData.add(mapList.get(4));
//            gridData.add(mapList.get(4));
//        }
//        if(faultInfo.get(5).get("checked").equals("1")){
//            charData.add(mapList.get(5));
//            gridData.add(mapList.get(5));
//        }
//        if(faultInfo.get(6).get("checked").equals("1")){
//            charData.add(mapList.get(6));
//            gridData.add(mapList.get(6));
//        }
//        if(faultInfo.get(7).get("checked").equals("1")){
//            charData.add(mapList.get(7));
//            gridData.add(mapList.get(7));
//        }
//        if(faultInfo.get(8).get("checked").equals("1")){
//            charData.add(mapList.get(8));
//            gridData.add(mapList.get(8));
//        }
//        if(faultInfo.get(9).get("checked").equals("1")){
//            charData.add(mapList.get(9));
//            gridData.add(mapList.get(9));
//        }
//        if(faultInfo.get(10).get("checked").equals("1")){
//            charData.add(mapList.get(10));
//            gridData.add(mapList.get(10));
//        }
//        if(faultInfo.get(11).get("checked").equals("1")){
//            charData.add(mapList.get(11));
//            gridData.add(mapList.get(11));
//        }
//        if(faultInfo.get(12).get("checked").equals("1")){
//            charData.add(mapList.get(12));
//            gridData.add(mapList.get(12));
//        }
//        if(faultInfo.get(13).get("checked").equals("1")){
//            charData.add(mapList.get(13));
//            gridData.add(mapList.get(13));
//        }
//        if(faultInfo.get(14).get("checked").equals("1")){
//            charData.add(mapList.get(14));
//            gridData.add(mapList.get(14));
//        }
//        if(faultInfo.get(15).get("checked").equals("1")){
//            charData.add(mapList.get(15));
//            gridData.add(mapList.get(15));
//        }
//        if(faultInfo.get(16).get("checked").equals("1")){
//            charData.add(mapList.get(16));
//            gridData.add(mapList.get(16));
//        }

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("charData", charData);
        map.put("gridData", gridData);
        return map;
    }

    public Result sendGetRequest(String url, HashMap<String, Long> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class, params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;

    }
}
