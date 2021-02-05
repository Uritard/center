package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.mp3.SpectrumMp3;
import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.module.device.entity.VoiceDeviceInfoDetail;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.INTERNAL;
import org.springframework.data.redis.core.RedisTemplate;

import java.beans.Encoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2021/2/5
 */
@Slf4j
public class VoiceAnalyseThread implements Runnable{
    private String voicePath;
    private Integer second;//单位毫秒
    private VoiceDeviceInfoDetail voiceDeviceInfoDetail;

    public VoiceAnalyseThread(String voicePath,Integer second,VoiceDeviceInfoDetail voiceDeviceInfoDetail){
        this.voicePath = voicePath;
        this.second = second;
        this.voiceDeviceInfoDetail = voiceDeviceInfoDetail;
    }

    @Override
    public void run() {
        List<Map<String,String>> reList = new ArrayList<>();
        VoiceAnalyseUtil voiceAnalyseUtil = new VoiceAnalyseUtil(voicePath);
        List<Integer> DBList = voiceAnalyseUtil.analyticalDecibels();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
        log.info("分贝数组："+DBList);
        //每一段的毫秒数；
        int hm = second/DBList.size();
        log.info("每一段的毫秒数："+hm);
        DBList.add(0);//补一个0 方便操作
        Integer warnDb = Integer.valueOf(voiceDeviceInfoDetail.getDbValue());
        List<Integer> timeList = new ArrayList<>();
        if(DBList.size()>0 && DBList.get(0)>warnDb){
            log.info("开始时间："+0);
            timeList.add(0);
        }
        for(int i = 1; i < DBList.size()-1;i++){
            try{
            if(DBList.get(i-1)<warnDb && DBList.get(i) >= warnDb){
                //记录开始时间
                log.info("开始时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                timeList.add(i);
            }
            if(DBList.get(i-1)>=warnDb && DBList.get(i) < warnDb){
                //记录结束时间
                log.info("结束时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                timeList.add(i);
            }
            }catch (Exception e){
            log.info("时间转化错误："+e);
            }
        }
        for(int j = 0;j < timeList.size();j=j+2){
            Map<String,String> map = new HashMap<>();
            map.put("startTime",simpleDateFormat.format(timeList.get(j)*hm- TimeZone.getDefault().getRawOffset()));
            map.put("endTime",simpleDateFormat.format(timeList.get(j+1)*hm- TimeZone.getDefault().getRawOffset()));
            map.put("time",simpleDateFormat.format((timeList.get(j+1)-timeList.get(j))*hm- TimeZone.getDefault().getRawOffset()));
            map.put("result",findBig(DBList,timeList.get(j),timeList.get(j+1)).toString());
            reList.add(map);
        }
        log.info("返回结果："+reList);
        //Constant.voiceAnalyseResult.put(voiceDeviceInfoDetail.getVoiceDeviceId()+":"+voicePath,reList);
    }
    private Integer findBig(List<Integer> list,int start,int end){
        Integer big = -1;
        for(int i = start;i<= end;i++){
            if(list.get(i)>big){
                big = list.get(i);
            }
        }
        return big;
    }
}
