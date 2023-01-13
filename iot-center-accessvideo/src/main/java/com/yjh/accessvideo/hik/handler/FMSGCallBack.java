package com.yjh.accessvideo.hik.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.device.entity.TWarnInfo;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/9
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class FMSGCallBack implements HCNetSDK.FMSGCallBack_V31 {
    private final String IMG_PATH;
    private final String IMG_PATH_URL;

    private AnalyseDataOperateService analyseDataOperateService;

    public FMSGCallBack(AnalyseDataOperateService analyseDataOperateService, String IMG_PATH, String IMG_PATH_URL) {
        this.IMG_PATH = IMG_PATH + "/dvrSilenceImg/";
        this.IMG_PATH_URL = IMG_PATH_URL + "/dvrSilenceImg/";

        File file = new File(IMG_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        this.analyseDataOperateService = analyseDataOperateService;
    }



    public boolean AlarmDataHandle(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
//        TWarnInfo tWarnInfo = new TWarnInfo().setDeviceId(-1L).setStdMeteId(-1L);
        Long recorderId = Constant.DVRMaps.get(pAlarmer.lUserID);
        TWarnInfo tWarnInfo = new TWarnInfo();
        String sAlarmType;
        String[] newRow = new String[3];
        //报警时间
        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String[] sIP = new String[2];

        sAlarmType = new String("lCommand=") + lCommand;
        //lCommand是传的报警类型
        List<JSONObject> rectList = new ArrayList<>();
        switch (lCommand) {
            case HCNetSDK.COMM_ISAPI_ALARM:
                //获取安全帽检测报警信息
                HCNetSDK.NET_DVR_ALARM_ISAPI_INFO netDvrAlarmIsapiInfo = new HCNetSDK.NET_DVR_ALARM_ISAPI_INFO();
                netDvrAlarmIsapiInfo.write();
                Pointer pointer = netDvrAlarmIsapiInfo.getPointer();
                pointer.write(0, pAlarmInfo.getByteArray(0, netDvrAlarmIsapiInfo.size()), 0, netDvrAlarmIsapiInfo.size());
                netDvrAlarmIsapiInfo.read();
                //根据图片信息数量，遍历数据
                int picNum = netDvrAlarmIsapiInfo.byPicturesNumber;
                System.out.println("picNum" + picNum);
                String pAlarmData = new String(netDvrAlarmIsapiInfo.pAlarmData.getByteArray(0,netDvrAlarmIsapiInfo.dwAlarmDataLen));
                JSONObject alarmData = JSON.parseObject(pAlarmData);

                if ("framesPeopleCounting".equals(alarmData.getString("eventType"))) {
                    dealDeviceInfo(tWarnInfo,recorderId,alarmData.getIntValue("channelID"));
                    if (Objects.isNull(tWarnInfo.getDeviceId()) || Objects.isNull(tWarnInfo.getInstanceId())) {
                        log.warn("当前摄像机未绑定测点！录像机号：{}，通道号：{}",recorderId, alarmData.get("channelID"));
                        return true;
                    }
                    JSONObject framesPeopleCounting = alarmData.getJSONObject("FramesPeopleCounting");
                    int framesPeopleCountingNum = framesPeopleCounting.getIntValue("framesPeopleCountingNum");
                    tWarnInfo.setWarnLevel(131).setWarnTime(new Date()).setWarnName("静默监视告警数据")
                        .setWarnContent("区域内人数：" + framesPeopleCountingNum).setConfMode(276).setDefectModel(450).setAlarmSource(689);

                    analyseDataOperateService.insertWarnInfo(tWarnInfo);

                } else if (alarmData.containsKey("Target")){
                    dealDeviceInfo(tWarnInfo,recorderId,alarmData.getIntValue("channel"));
                    if (Objects.isNull(tWarnInfo.getDeviceId()) || Objects.isNull(tWarnInfo.getInstanceId())) {
                        log.warn("当前摄像机未绑定测点！录像机号：{}，通道号：{}",recorderId, alarmData.get("channel"));
                        return true;
                    }
                    JSONArray targetList = alarmData.getJSONArray("Target");
                    System.out.println(alarmData);
                    if (targetList.size() != 0) {
                        for (int i = 0; i != targetList.size(); i++) {
                            JSONObject jsonObject1 = targetList.getJSONObject(i);
                            JSONArray tempList = jsonObject1.getJSONArray("rect");
                            for (int j = 0; j != tempList.size(); j++) {
                                rectList.add(tempList.getJSONObject(j));
                            }
                        }
                    }
                    System.out.println(alarmData.toJSONString());
                    for (int i = 0; i < picNum; i++) {
                        //获取报警信息中的图片信息
                        HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA alarmIsApiPicData = new HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA();
                        Pointer totalPointer = netDvrAlarmIsapiInfo.pPicPackData;
                        Pointer aPointer = alarmIsApiPicData.getPointer();
                        aPointer.write(0, totalPointer.getByteArray((long)i * alarmIsApiPicData.size(), alarmIsApiPicData.size()), 0,
                            alarmIsApiPicData.size());
                        alarmIsApiPicData.read();
                        Pointer picDataPointer = alarmIsApiPicData.pPicData;
                        byte[] bytes1 = picDataPointer.getByteArray(0, alarmIsApiPicData.dwPicLen);
                        String fileName = new String(alarmIsApiPicData.szFilename).trim() + "_" + new SimpleDateFormat("yyyyMMddHHmmss")
                            .format(new Date()) + ".jpg";

                        System.out.println(IMG_PATH + fileName);
                        if ("background_image".equals(new String(alarmIsApiPicData.szFilename).trim())) {
                            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes1)) {
                                File file = new File(IMG_PATH + fileName);
                                tWarnInfo.setImagePath(IMG_PATH_URL + fileName);
                                if (rectList.size() != 0) {
                                    BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
                                    int width = bufferedImage.getWidth();
                                    int height = bufferedImage.getHeight();
                                    Graphics graphics = bufferedImage.getGraphics();
                                    rectList.forEach(rect -> {
                                        int x = rect.getBigDecimal("x").multiply(new BigDecimal(width)).intValue();
                                        int y = rect.getBigDecimal("y").multiply(new BigDecimal(height)).intValue();
                                        int drawWidth = rect.getBigDecimal("width").multiply(new BigDecimal(width)).intValue();
                                        int drawHeight = rect.getBigDecimal("height").multiply(new BigDecimal(height)).intValue();
                                        graphics.setColor(Color.RED);
                                        graphics.drawRect(x, y, drawWidth, drawHeight);
                                        graphics.dispose();
                                    });
                                    ImageIO.write(bufferedImage, "jpg", file);
                                }
                            } catch (IOException e) {
                                log.error(e.getMessage(), e.getCause());
                            }
                        }
                    }
                    tWarnInfo.setWarnLevel(131).setWarnTime(new Date()).setWarnName("静默监视告警数据").setWarnContent("未佩戴安全帽").setConfMode(276)
                        .setDefectModel(450).setAlarmSource(689);
                    analyseDataOperateService.insertWarnInfo(tWarnInfo);
                }
                break;

            case HCNetSDK.COMM_ALARM_RULE:
                HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                strVcaAlarm.write();
                Pointer pVcaInfo = strVcaAlarm.getPointer();
                pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                strVcaAlarm.read();
                dealDeviceInfo(tWarnInfo,recorderId,strVcaAlarm.struDevInfo.byIvmsChannel);
                if (Objects.isNull(tWarnInfo.getDeviceId()) || Objects.isNull(tWarnInfo.getInstanceId())) {
                    log.warn("当前摄像机未绑定测点！录像机号：{}，通道号：{}",recorderId, strVcaAlarm.struDevInfo.byIvmsChannel);
                    return true;
                }
                String warnContent = "";
                switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_ADV_TRAVERSE_PLANE:
                        sAlarmType = sAlarmType + new String("：穿越警戒面") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "穿越警戒面";
                        break;
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_ENTER_AREA:
                        sAlarmType = sAlarmType + new String("：目标进入区域") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "目标进入区域";
                        break;
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_EXIT_AREA:
                        sAlarmType = sAlarmType + new String("：目标离开区域") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "目标离开区域";
                        break;
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_INTRUSION:
                        sAlarmType = sAlarmType + new String("区域入侵侦测：") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "区域入侵侦测";
                        break;
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_LEAVE_POSITION:
                        sAlarmType = sAlarmType + new String("离岗：") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "离岗";
                        break;
                    case HCNetSDK._VCA_RULE_EVENT_TYPE_EX_.ENUM_VCA_EVENT_PLAY_CELLPHONE:
                        sAlarmType = sAlarmType + new String("玩手机检测：") + "，" + "_wPort:" + strVcaAlarm.struDevInfo.wPort + "_byChannel:"
                            + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP："
                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        warnContent += "玩手机检测";
                        HCNetSDK.NET_VCA_TARGET_INFO netVcaTargetInfo = strVcaAlarm.struTargetInfo;
                        HCNetSDK.NET_VCA_RECT netVcaRect = netVcaTargetInfo.struRect;
                        rectList.add(JSON.parseObject(JSON.toJSONString(netVcaRect)));
                        break;
                    default:
                        sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：") + strVcaAlarm.struRuleInfo.wEventTypeEx + "_wPort:"
                            + strVcaAlarm.struDevInfo.wPort + "_byChannel:" + strVcaAlarm.struDevInfo.byChannel + "_byIvmsChannel:"
                            + strVcaAlarm.struDevInfo.byIvmsChannel + "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                        break;
                }

                if (strVcaAlarm.dwPicDataLen > 0) {
                    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String fileName =
                        String.valueOf(strVcaAlarm.struRuleInfo.wEventTypeEx).trim() + "_" + new SimpleDateFormat("yyyyMMddHHmmss")
                            .format(new Date()) + ".jpg";
                    tWarnInfo.setImagePath(IMG_PATH_URL + fileName);
                    byte[] imgByte = strVcaAlarm.pImage.getByteArray(0, strVcaAlarm.dwPicDataLen);
                    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imgByte)) {
                        File file = new File(IMG_PATH + fileName);
                        BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
                        if (rectList.size() != 0) {
                            int width = bufferedImage.getWidth();
                            int height = bufferedImage.getHeight();
                            Graphics graphics = bufferedImage.getGraphics();
                            rectList.forEach(rect -> {
                                int x = rect.getBigDecimal("fX").multiply(new BigDecimal(width)).intValue();
                                int y = rect.getBigDecimal("fY").multiply(new BigDecimal(height)).intValue();
                                int drawWidth = rect.getBigDecimal("fWidth").multiply(new BigDecimal(width)).intValue();
                                int drawHeight = rect.getBigDecimal("fHeight").multiply(new BigDecimal(height)).intValue();
                                graphics.setColor(Color.RED);
                                graphics.drawRect(x, y, drawWidth, drawHeight);
                                graphics.dispose();
                            });
                        }
                        ImageIO.write(bufferedImage, "jpg", file);
                    } catch (IOException e) {
                        System.out.println(e);
                    }

                }

                tWarnInfo.setWarnLevel(131).setWarnTime(new Date()).setWarnName("静默监视告警数据").setWarnContent(warnContent).setConfMode(276)
                    .setDefectModel(450).setAlarmSource(689);
                analyseDataOperateService.insertWarnInfo(tWarnInfo);
                break;

            case HCNetSDK.COMM_ALARM_PDC:
                HCNetSDK.NET_DVR_PDC_ALRAM_INFO strPDCResult = new HCNetSDK.NET_DVR_PDC_ALRAM_INFO();
                strPDCResult.write();
                Pointer pPDCInfo = strPDCResult.getPointer();
                pPDCInfo.write(0, pAlarmInfo.getByteArray(0, strPDCResult.size()), 0, strPDCResult.size());
                strPDCResult.read();
                dealDeviceInfo(tWarnInfo,recorderId, strPDCResult.struDevInfo.byIvmsChannel);
                if (Objects.isNull(tWarnInfo.getDeviceId()) || Objects.isNull(tWarnInfo.getInstanceId())) {
                    log.warn("当前摄像机未绑定测点！录像机号：{}，通道号：{}",recorderId, strPDCResult.struDevInfo.byIvmsChannel);
                    return true;
                }
                if (strPDCResult.byMode == 0) {
                    strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATFRAME.class);
                    sAlarmType = sAlarmType + "：客流量统计，进入人数：" + strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum + ", byMode:"
                        + strPDCResult.byMode + ", dwRelativeTime:" + strPDCResult.uStatModeParam.struStatFrame.dwRelativeTime
                        + ", dwAbsTime:" + strPDCResult.uStatModeParam.struStatFrame.dwAbsTime;
                }
                if (strPDCResult.byMode == 1) {
                    strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATTIME.class);
                    String strtmStart = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwYear) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMonth) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwDay) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwHour) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMinute) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwSecond);
                    String strtmEnd = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwYear) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMonth) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwDay) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwHour) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMinute) + String
                        .format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwSecond);
                    sAlarmType = sAlarmType + "：客流量统计，进入人数：" + strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum + ", byMode:"
                        + strPDCResult.byMode + ", tmStart:" + strtmStart + ",tmEnd :" + strtmEnd;
                }

                tWarnInfo.setWarnLevel(131).setWarnTime(new Date()).setWarnName("静默监视告警数据").setWarnContent("客流量统计，进入人数：" + strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum).setConfMode(276)
                    .setDefectModel(450).setAlarmSource(689);
                analyseDataOperateService.insertWarnInfo(tWarnInfo);
                break;
            default:

                break;
        }

        return true;
    }

    private void dealDeviceInfo(TWarnInfo tWarnInfo,Long recorderId, int channelId) {

        Map<String,Object> result = analyseDataOperateService.selectCameraByRecorderIdAndChannelId(recorderId,channelId);
        if (result != null && result.size() != 0) {
            tWarnInfo.setDeviceId((Long) result.get("device_id"));
            tWarnInfo.setStdMeteId((Long) result.get("device_mete_id"));
            tWarnInfo.setCunstomId((String) result.get("custom_id"));
            tWarnInfo.setInstanceId((Long) result.get("instance_id"));
        }

    }

    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {

        return AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
    }
}