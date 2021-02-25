package com.yjh.platform.module.device.service;

import com.sun.jna.Pointer;
import com.yjh.platform.common.tradio.NET_TRADIO_DEVICEINFO;
import com.yjh.platform.common.tradio.TradioLibrary;
import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.LongBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.device.entity.VoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceInfoDetail;
import com.yjh.platform.module.user.dao.TSysParamDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;

/**
* @author lqh
* @since 2020-12-01
*/
@Service
public class TVoiceDeviceService{

    @Autowired
    private TVoiceDeviceDao tVoiceDeviceDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TSysParamDao tSysParamDao;

    private Logger log = LoggerFactory.getLogger(TVoiceDeviceService.class);

    private static TradioLibrary sdk_= TradioLibrary.INSTANCE;;

    @Transactional(rollbackFor = Exception.class)
    public int add(TVoiceDevice tVoiceDevice) {
        return this.tVoiceDeviceDao.add(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String voiceDeviceId) {
        return this.tVoiceDeviceDao.deleteByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TVoiceDevice tVoiceDevice) {
        return this.tVoiceDeviceDao.update(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public TVoiceDevice selectByPrimaryId(String voiceDeviceId) {
        return this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> select(String voiceDeviceId, String voiceDeviceName, Long stdDeviceId, String deviceType, String configId,Long upRegionId) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId, upRegionId);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> selectByPage(TVoiceDevice tVoiceDevice) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.selectByPage(tVoiceDevice);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public String voiceFileGenerate(Long voiceDeviceId) {
        String voicePath = "F:\\workspace\\";

        if (sdk_.NET_TRADIO_Init() == 0) {
            System.out.println("SDK初始化成功");
        } else {
            System.out.println("SDK初始化失败");
        }

        LongBuffer hd = LongBuffer.allocate(1);
        if(sdk_.NET_TRADIO_CreateDevice(hd) == 0){
            System.out.println("创建设备成功");
        }else{
            System.out.println("创建设备失败");
        }

        final int[] byteArratTemLength = {0};
        long hdForData = hd.get();
        sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
            @Override
            public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {
                log.info("收到数据：charPtr1=" + data + "，int1=" + len + "，int2=" + channel + "，int3=" + db
                        + "，long1=" + dev + "，sample_rate=" + sample_rate);

                byte[] bytesArrayTem = new byte[len];
                StringBuilder StrArrayTem = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    byte[] byteOne = new byte[1];
                    byteOne[0] = data.getByte(i);
                    System.arraycopy(byteOne, 0, bytesArrayTem, i, 1);
                    StrArrayTem.append(String.format("%02x ", data.getByte(i)));
                }

                log.info("receiveOriginalDataArray:" + StrArrayTem);

                if (channel==0) {
                    try {
                        File file = new File(voicePath);
                        if (!file.exists()) { file.mkdirs(); }
                        File tempWav = new File(file, "voice"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +".aac");
                        if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                        FileOutputStream fos = new FileOutputStream(tempWav, true);
                        fos.write(bytesArrayTem, 0, bytesArrayTem.length);
                        fos.flush();
                        fos.close();
                        byteArratTemLength[0] = byteArratTemLength[0]+len;
                    } catch (IOException e) { e.getMessage(); }
                }
            }
        }, 0);
        log.info("byteArrayLength: "+byteArratTemLength[0]);

        NET_TRADIO_DEVICEINFO dev = new NET_TRADIO_DEVICEINFO();
        if (sdk_.NET_TRADIO_Login(hdForData, "192.168.10.127", 38000, "admin", "123456", dev) == 0) {
            System.out.println("注册成功");
        }else {
            System.out.println("注册失败");
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int id = 0;
        if (sdk_.NET_TRADIO_Logout(id) != 0) {
            System.out.println("设备注销成功");
        } else {
            System.out.println("设备注销失败");
        }

        sdk_.NET_TRADIO_Clear();


        return voicePath;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TVoiceDevice> list) {
        return this.tVoiceDeviceDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String voiceDeviceId) {
    List<String> list1= Arrays.asList(voiceDeviceId.split(","));
    return this.tVoiceDeviceDao.batchDelete(list1);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<VoiceDevice> selectVoiceDeviceTree(String voiceDeviceName) {
//        List<VoiceDevice> re = new ArrayList<>();
//        VoiceDevice voiceDeviceTree = new VoiceDevice();
//        voiceDeviceTree.setInfoType("tree");
//        voiceDeviceTree.setLabel("音频设备树");
//        voiceDeviceTree.setId("-1");
//        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.selectAll();
//        List<VoiceDevice> child = new ArrayList<>();
//        for (TVoiceDevice item:tVoiceDeviceList) {
//            VoiceDevice childDevice = new VoiceDevice();
//            childDevice.setId(item.getVoiceDeviceId());
//            childDevice.setLabel(item.getVoiceDeviceName());
//            childDevice.setUpId("1");
//            childDevice.setInfoType("device");
//            childDevice.setUpName("音频设备树");
//            child.add(childDevice);
//        }
//        voiceDeviceTree.setChildren(child);
//        re.add(voiceDeviceTree);
//        return re;
        List<VoiceDevice> listTree = new ArrayList<>();
        listTree = tVoiceDeviceDao.selectAll(voiceDeviceName);
        List<VoiceDevice> areaInfoCountryList = new ArrayList<>();
        for(Iterator<VoiceDevice> it = listTree.iterator(); it.hasNext();){
            VoiceDevice areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && "-1".equals(areaInfoMap.getUpId())) {
                VoiceDevice areaInfoCountry = new VoiceDevice();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        String realPath = tSysParamDao.selectByParamType("relativeVoicePath").getContent();
        String absPath  = tSysParamDao.selectByParamType("absVoicePath").getContent();
        diGui(areaInfoCountryList, listTree,realPath,absPath);
        return areaInfoCountryList;
    }
    private void diGui(List<VoiceDevice> areaInfoList, List<VoiceDevice> listTree,String realPath,String absPath) {
        for(VoiceDevice areaInfo : areaInfoList){
            List<VoiceDevice> childrenList = new ArrayList<>();
            for(Iterator<VoiceDevice> it = listTree.iterator();it.hasNext();){
                VoiceDevice areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    VoiceDevice areaInfoTem = new VoiceDevice();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    List<VoiceDevice> fileList = new ArrayList<>();
                    if("device".equals(areaInfoTem.getInfoType())){
                        boolean flag = false;
                        //File file = new File("D:/code/qhTest"+"/"+areaInfoTem.getId());
                        File file = new File(absPath+"/"+areaInfoTem.getId());
                        File[] tempList = file.listFiles();
                        if(tempList != null && tempList.length>0){
                            for (int i = 0; i < tempList.length; i++) {
                                if (tempList[i].isFile()) {
                                    VoiceDevice voiceDevice = new VoiceDevice();
                                    voiceDevice.setUpId(areaInfoTem.getId());
                                    voiceDevice.setUpName(areaInfoTem.getLabel());
                                    voiceDevice.setLabel(tempList[i].getName());
                                    voiceDevice.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+tempList[i].getName());
                                    voiceDevice.setInfoType("file");
                                    fileList.add(voiceDevice);
                                }
                            }
                        }

                    }
                    areaInfoTem.setChildren(fileList);
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree,realPath,absPath);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> voiceAnalyse(String voicePath,String voiceDeviceId) throws Exception{
//        if(Constant.voiceAnalyseResult.get(voiceDeviceId+":"+voicePath) != null){
//            //此文件已经分析过了
//        }
        //String path = "D:/code/qhTest";
        String path = tSysParamDao.selectByParamType("absVoicePath").getContent();
        MultimediaObject multimediaObject = new MultimediaObject(new File(path+"/"+voiceDeviceId+"/"+voicePath));
        MultimediaInfo info = multimediaObject.getInfo();
        Long playTime = info.getDuration();
        VoiceDeviceInfoDetail voiceDeviceInfoDetail = tVoiceDeviceDao.selectVoiceInfo(voiceDeviceId);
//        VoiceAnalyseThread voiceAnalyseThread = new VoiceAnalyseThread(voicePath,playTime.intValue(),voiceDeviceInfoDetail);
//        Thread thread = new Thread(voiceAnalyseThread);
//        thread.setDaemon(true);
//        thread.start();
        return voiceAnalyse(path+"/"+voiceDeviceId+"/"+voicePath,playTime.intValue(),voiceDeviceInfoDetail);
    }

    private List<Map<String,String>> voiceAnalyse(String voicePath,Integer second,VoiceDeviceInfoDetail voiceDeviceInfoDetail){
        log.info("文件："+voicePath);
        List<Map<String,String>> reList = new ArrayList<>();
        VoiceAnalyseUtil voiceAnalyseUtil = new VoiceAnalyseUtil(voicePath);
        List<Integer> DBList = voiceAnalyseUtil.analyticalDecibels();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
        log.info("分贝数组："+DBList);
        //每一段的毫秒数；
        float hm = second/DBList.size();
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
        if(timeList.size() == 1){
            //只有开始没有结束
            timeList.add(DBList.size()-1);
            log.info("结束时间："+simpleDateFormat.format((DBList.size()-1)*hm- TimeZone.getDefault().getRawOffset()));
        }
        DecimalFormat df1 = new DecimalFormat("#####0.000");
        try{
        for(int j = 0;j < timeList.size();j=j+2){
            Map<String,String> map = new HashMap<>();
            map.put("startTime",simpleDateFormat.format(timeList.get(j)*hm- TimeZone.getDefault().getRawOffset()));
            map.put("endTime",simpleDateFormat.format(timeList.get(j+1)*hm- TimeZone.getDefault().getRawOffset()));
            if(timeList.get(j+1) == (DBList.size()-1)){
                //整个文件满足
                map.put("endTime",simpleDateFormat.format(second- TimeZone.getDefault().getRawOffset()));
                map.put("time",String.valueOf(second/1000F));
            } else {
                map.put("endTime",simpleDateFormat.format(timeList.get(j+1)*hm- TimeZone.getDefault().getRawOffset()));
                map.put("time",df1.format(((timeList.get(j+1)-timeList.get(j))*hm)/1000F));
            }

            map.put("result",findBig(DBList,timeList.get(j),timeList.get(j+1)).toString());
            reList.add(map);
        }
        }catch (Exception e){
            log.info("时间转化错误："+e);
        }
        log.info("返回结果："+reList);
        return  reList;
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

