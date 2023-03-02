package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.audiodevice.impl.AudioDeviceFactory;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.LittlePriorityQueue;
import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.dao.TSysParamDao;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author lqh
 * @since 2020-12-01
 */
@Service
public class TVoiceDeviceService{

    @Autowired
    private TVoiceDeviceDao tVoiceDeviceDao;
    @Autowired
    private TSysParamDao tSysParamDao;
    @Autowired
    private TStdDeviceDao tStdDeviceDao;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AudioDeviceManager audioDeviceManager;
    @Autowired
    private AudioDeviceFactory audioDeviceFactory;
    @Autowired
    private TVoiceAsyncService tVoiceAsyncService;

    private Logger log = LoggerFactory.getLogger(TVoiceDeviceService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(VoiceDeviceAllInfoDetail tVoiceDevice) {
//        List<TStdDevice> tStdDevice = tStdDeviceDao.selectByPrimaryId(tVoiceDevice.getStdDeviceId());
//        if(tStdDevice.size()!=0){
//            tVoiceDevice.setDeviceType(tStdDevice.get(0).getDeviceType().toString());
//            //tVoiceDevice.setStationId(tStdDevice.getS)
//        }else {
//            return -1;
//        }
        //tVoiceDevice.setState("未知");
        if(ping(tVoiceDevice.getFtpUrl())){
            tVoiceDevice.setState("在线");
        }else {
            tVoiceDevice.setState("离线");
        }
        if(tVoiceDevice.getVoiceCode() != null && !"".equals(tVoiceDevice.getVoiceCode())){
            audioDeviceManager.registerAudioDevice(audioDeviceFactory.newAudioDevice(tVoiceDevice.getVoiceCode()));
        }
        this.tVoiceDeviceDao.addConf(tVoiceDevice);
        return this.tVoiceDeviceDao.add(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long voiceDeviceId) {
        VoiceDeviceAllInfoDetail voiceDeviceInfoDetail = this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
        this.tVoiceDeviceDao.deleteConf(voiceDeviceInfoDetail.getConfigId());
        String recordKey = "is_record_open_state:"+String.valueOf(voiceDeviceId);
        redisTemplate.opsForHash().delete(recordKey, "openState", "voiceDeviceId");
        return this.tVoiceDeviceDao.deleteByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(VoiceDeviceAllInfoDetail tVoiceDevice) {
//        List<TStdDevice> tStdDevice = tStdDeviceDao.selectByPrimaryId(tVoiceDevice.getStdDeviceId());
//        if(tStdDevice.size() != 0){
//            tVoiceDevice.setDeviceType(tStdDevice.get(0).getDeviceType().toString());
//            //tVoiceDevice.setStationId(tStdDevice.getS)
//        }else {
//            return -1;
//        }
        //tVoiceDevice.setState("未知");
        if(ping(tVoiceDevice.getFtpUrl())){
             tVoiceDevice.setState("在线");
        }else {
             tVoiceDevice.setState("离线");
        }
        if(tVoiceDevice.getVoiceCode() != null && !"".equals(tVoiceDevice.getVoiceCode())){
            VoiceDeviceAllInfoDetail old = this.selectByPrimaryId(tVoiceDevice.getVoiceDeviceId());
            if(!old.getVoiceCode().equals(tVoiceDevice.getVoiceCode())){
                audioDeviceManager.registerAudioDevice(audioDeviceFactory.newAudioDevice(tVoiceDevice.getVoiceCode()));
            }
        }
        this.tVoiceDeviceDao.updateConf(tVoiceDevice);
        return this.tVoiceDeviceDao.update(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public VoiceDeviceAllInfoDetail selectByPrimaryId(Long voiceDeviceId) {
        return this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> select(Long voiceDeviceId, String voiceDeviceName, Long stdDeviceId, String deviceType, Long configId,
        Long upRegionId, String voiceType, String voiceModel, String voiceFactory) {

        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId,
            upRegionId, voiceType, voiceModel, voiceFactory);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result selectByPage(String voiceDeviceName,String deviceType,Long upRegionId,
        String voiceType, String voiceModel, String voiceFactory,int pageNum,int pageSize) {
        Result result= new Result();
        Map<String, Object> resultMap = new HashMap<>();
        if("-1".equals(deviceType)){
            deviceType = null;
        }
        List<Long> list = tStdRegionDao.selectDownId(upRegionId);
        if(upRegionId != null){
            list.add(upRegionId);
        }
        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<VoiceDeviceAllInfoDetail> tVoiceDeviceList = tVoiceDeviceDao.selectByPage(voiceDeviceName,deviceType,list,upRegionId,
            voiceType, voiceModel, voiceFactory);
        List<Future<VoiceDeviceAllInfoDetail>> futureList = new ArrayList<>();
        for(VoiceDeviceAllInfoDetail item:tVoiceDeviceList){
            // 异步线程执行判断声纹是否在线
            futureList.add(tVoiceAsyncService.syncVoiceState(item));

            Map<String,String> isOpen = redisTemplate.opsForHash().entries("is_record_open_state:"+item.getVoiceDeviceId());
            if(isOpen != null && isOpen.size()>0){
                item.setOpenState(isOpen.get("openState"));
            }else {
                item.setOpenState("关闭");
            }
        }

        // 等待子线程执行完
        try {
            while (true) {
                Thread.sleep(800);
                boolean isAllDone = true;
                for (Future<VoiceDeviceAllInfoDetail> future : futureList) {
                    if (null == future || !future.isDone()) {
                        isAllDone = false;
                    }
                }
                if (isAllDone) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        resultMap.put("count", page.getTotal());
        resultMap.put("list", tVoiceDeviceList);
        result.setData(resultMap);
        return result;
    }

    public boolean ping(String ipAddress) {
        try {
            int timeOut = 3000;  //超时应该在3钞以上
            boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);
            // 当返回值是true时，说明host是可用的，false则不可。
            return status;
        }catch (Exception e){
            log.error("获取状态失败："+e);
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<VoiceDeviceAllInfoDetail> list) {
        return this.tVoiceDeviceDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String voiceDeviceId) {
        List<String> list1= Arrays.asList(voiceDeviceId.split(","));
        for(String item:list1){
            this.deleteByPrimaryId(Long.valueOf(item));
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<VoiceDevice> selectVoiceDeviceTree(String voiceDeviceName, Long userId) {
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
        listTree = tVoiceDeviceDao.selectAll(voiceDeviceName, userId);
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
                    areaInfoTem.setDbValue(areaInfoMap.getDbValue());
                    areaInfoTem.setFValue(areaInfoMap.getFValue());
                    List<VoiceDevice> channelList = new ArrayList<>();
                    if("device".equals(areaInfoTem.getInfoType())){
                        //todo 记得加上日期这一层级日期
                        boolean flag = false;

                        File channelFile = new File(absPath+"/"+areaInfoTem.getId());
                        File[] channelFileList = channelFile.listFiles();
                        if(channelFileList != null && channelFileList.length>0){
                            for (int i = 0; i < channelFileList.length; i++) {
                                if (channelFileList[i].isDirectory()) {//通道文件夹
                                    VoiceDevice channel = new VoiceDevice();
                                    channel.setUpId(areaInfoTem.getId());
                                    channel.setUpName(areaInfoTem.getLabel());
                                    channel.setLabel(channelFileList[i].getName());
                                    channel.setId(channelFileList[i].getName());
                                    //voiceDevice.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+tempList[i].getName());
                                    channel.setInfoType("channel");
                                    channel.setDbValue(areaInfoTem.getDbValue());
                                    channel.setFValue(areaInfoTem.getFValue());
                                    //查询此文件夹下的所有文件
                                    List<VoiceDevice> dateList = new ArrayList<>();
                                    File fileForDate = new File(absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName());
                                    List<File> voiceDateList = getFileDirectorySort(fileForDate);
                                    if(voiceDateList != null && voiceDateList.size()>0) {
                                        for (int j = 0; j < voiceDateList.size(); j++) {
                                            if (voiceDateList.get(j).isDirectory()) {//日期文件夹
                                                VoiceDevice date = new VoiceDevice();
                                                date.setUpId(channel.getId());
                                                date.setUpName(channel.getLabel());
                                                date.setLabel(voiceDateList.get(j).getName());
                                                date.setId(voiceDateList.get(j).getName());
                                                //date.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName());
                                                date.setInfoType("date");
                                                date.setDbValue(channel.getDbValue());
                                                date.setFValue(channel.getFValue());
                                                //查询此文件夹下的所有文件
                                                List<VoiceDevice> fileList = new ArrayList<>();
                                                String path = absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList.get(j).getName();
//                                                File fileForFile = new File(absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName());
//                                                File[] voiceFileList = fileForFile.listFiles();
                                                List<File> voiceFileList = getFileSort(path);
                                                if(voiceFileList != null && voiceFileList.size()>0) {
                                                    for (int k = 0; k < voiceFileList.size(); k++) {
                                                        if (voiceFileList.get(k).isFile() && voiceFileList.get(k).getName().contains(".wav")) {//音频文件
                                                            VoiceDevice voiceFile = new VoiceDevice();
                                                            voiceFile.setUpId(date.getId());
                                                            voiceFile.setUpName(date.getLabel());
                                                            voiceFile.setId(voiceFileList.get(k).getName());
                                                            voiceFile.setLabel(voiceFileList.get(k).getName());
                                                            voiceFile.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList.get(j).getName()+"/"+voiceFileList.get(k).getName());
                                                            voiceFile.setInfoType("file");
                                                            voiceFile.setDbValue(date.getDbValue());
                                                            voiceFile.setFValue(date.getFValue());
                                                            fileList.add(voiceFile);
                                                        }
                                                    }

                                                }
                                                date.setChildren(fileList);
                                                dateList.add(date);
                                            }
                                        }
                                    }
                                    channel.setChildren(dateList);
                                    channelList.add(channel);
                                }
                            }
                        }

                    }
                    areaInfoTem.setChildren(channelList);
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree,realPath,absPath);
            }
        }
    }

    private List<File> getFileSort(String path) {

        List<File> list = getFiles(path, new ArrayList<File>());

        if (list != null && list.size() > 0) {

            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });

        }

        return list;
    }

    private List<File> getFileDirectorySort(File file) {

        List<File> list = Arrays.asList(file.listFiles());

        if (list != null && list.size() > 0) {

            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });

        }

        return list;
    }

    private  List<File> getFiles(String realpath, List<File> files) {

        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> voiceAnalyse(String voicePath) throws Exception{
//        if(Constant.voiceAnalyseResult.get(voiceDeviceId+":"+voicePath) != null){
//            //此文件已经分析过了
//        }
        Long voiceDeviceId = null;
        String absPath = tSysParamDao.selectByParamType("absVoicePath").getContent();
        String realPath = tSysParamDao.selectByParamType("relativeVoicePath").getContent();


        String[] getId = voicePath.replaceAll(realPath,"").split("/");
        if(getId != null && getId.length>2){
            if("".equals(getId[0])){
                voiceDeviceId = Long.valueOf(getId[1]);
            }else {
                voiceDeviceId = Long.valueOf(getId[0]);
            }
        }
        voicePath = voicePath.replaceAll(realPath,absPath);
//        voicePath = "D:/code/qhTest/10/1/2021-02-05/11.wav";
        MultimediaObject multimediaObject = new MultimediaObject(new File(voicePath));
        MultimediaInfo info = multimediaObject.getInfo();
        Long playTime = info.getDuration();
        VoiceDeviceInfoDetail voiceDeviceInfoDetail = tVoiceDeviceDao.selectVoiceInfo(voiceDeviceId);
        log.info("voiceDeviceInfoDetail: "+voiceDeviceInfoDetail);
//        VoiceAnalyseThread voiceAnalyseThread = new VoiceAnalyseThread(voicePath,playTime.intValue(),voiceDeviceInfoDetail);
//        Thread thread = new Thread(voiceAnalyseThread);
//        thread.setDaemon(true);
//        thread.start();
        if(voiceDeviceId == null){
            return null;
        }
        return voiceAnalyse(voicePath,playTime.intValue(),voiceDeviceInfoDetail);
    }

    private List<Map<String,String>> voiceAnalyse(String voicePath,Integer second,VoiceDeviceInfoDetail voiceDeviceInfoDetail){
        log.info("文件："+voicePath);
        List<Map<String,String>> reList = new ArrayList<>();
        List<Integer> DBList = null;
        try {
            VoiceAnalyseUtil voiceAnalyseUtil = new VoiceAnalyseUtil(voicePath);
            DBList = voiceAnalyseUtil.analyticalDecibels();
        }catch (Exception e){
            throw new BusinessException(209,"音频文件读取异常");
        }
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
                    //log.info("开始时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                    timeList.add(i);
                }
                if(DBList.get(i-1)>=warnDb && DBList.get(i) < warnDb){
                    //记录结束时间
                    //log.info("结束时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                    timeList.add(i);
                }
            }catch (Exception e){
                log.info("时间转化错误："+e);
            }
        }
        if(timeList.size()%2 == 1){
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

    /**
     * 获取第3大的数据，避免噪点数据影响结果
     */
    public Integer findBigPeakClipp(List<Integer> list, int start, int end, int size){
        LittlePriorityQueue<Integer> queue = new LittlePriorityQueue<>(size, size, 0);
        for(int i = start;i<= end;i++){
            queue.insert(list.get(i));
        }
        return queue.pop();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> frequencyAnalyse(String frequencyPath) throws Exception{
        Long voiceDeviceId = null;
        String absPath = tSysParamDao.selectByParamType("absVoicePath").getContent();
        String realPath = tSysParamDao.selectByParamType("relativeVoicePath").getContent();
        String[] getId = frequencyPath.replaceAll(realPath,"").split("/");
        if(getId != null && getId.length>2){
            if("".equals(getId[0])){
                voiceDeviceId = Long.valueOf(getId[1]);
            }else {
                voiceDeviceId = Long.valueOf(getId[0]);
            }
        }
        frequencyPath = frequencyPath.replaceAll(realPath,absPath);
//        frequencyPath= "D:/code/qhTest/10/1/2021-02-05/11.wav";
        MultimediaObject multimediaObject = new MultimediaObject(new File(frequencyPath));
        MultimediaInfo info = multimediaObject.getInfo();
        Long playTime = info.getDuration();
        VoiceDeviceInfoDetail voiceDeviceInfoDetail = tVoiceDeviceDao.selectFrequencyInfo(voiceDeviceId);
        if(voiceDeviceId == null){
            return null;
        }
        return voiceAnalyses(frequencyPath,playTime.intValue(),voiceDeviceInfoDetail);
    }

    private List<Map<String,String>> voiceAnalyses(String voicePath,Integer second,VoiceDeviceInfoDetail voiceDeviceInfoDetail){
        log.info("文件："+voicePath);
        List<Map<String,String>> reList = new ArrayList<>();
        List<Integer> DBList = null;
        try {
            VoiceAnalyseUtil voiceAnalyseUtil = new VoiceAnalyseUtil(voicePath);
             DBList = voiceAnalyseUtil.analyticalDecibelsPl();
        }catch (Exception e){
            throw new BusinessException(209,"音频文件读取异常");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss.SSS");
        log.info("频率数组："+DBList);
        //每一段的毫秒数；
        float hm = second/DBList.size();
        log.info("每一段的毫秒数："+hm);
        DBList.add(0);//补一个0 方便操作
        Integer warnDb = Integer.valueOf(voiceDeviceInfoDetail.getFValue());
        List<Integer> timeList = new ArrayList<>();
        if(DBList.size()>0 && DBList.get(0)>warnDb){
            log.info("开始时间："+0);
            timeList.add(0);
        }
        for(int i = 1; i < DBList.size()-1;i++){
            try{
                if(DBList.get(i-1)<warnDb && DBList.get(i) >= warnDb){
                    //记录开始时间
                    //log.info("开始时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                    timeList.add(i);
                }
                if(DBList.get(i-1)>=warnDb && DBList.get(i) < warnDb){
                    //记录结束时间
                    //log.info("结束时间："+simpleDateFormat.format(i*hm- TimeZone.getDefault().getRawOffset()));
                    timeList.add(i);
                }
            }catch (Exception e){
                log.info("时间转化错误："+e);
            }
        }
        if(timeList.size()%2 == 1){
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

                map.put("result", findBigPeakClipp(DBList, timeList.get(j), timeList.get(j+1), 3).toString());
                reList.add(map);
            }
        }catch (Exception e){
            log.info("时间转化错误：", e);
        }
        log.info("返回结果："+reList);
        return  reList;
    }











    //查询音频设备的所有信息
    @Transactional(rollbackFor = Exception.class)
    public List<VoiceDeviceAllInfo> selectVoiceDeviceInfo(){
        return this.tVoiceDeviceDao.selectVoiceDeviceInfo();
    }

    //从pms同步设备信息
    @Transactional(rollbackFor = Exception.class)
    public int synchronizeFromPMS(String pmsId,Long voiceDeviceId) throws Exception{
        //在那时理解为从无到有
        VoiceDeviceAllInfoDetail voiceDeviceAllInfoDetail;
        if(voiceDeviceId == null){
            voiceDeviceAllInfoDetail=new VoiceDeviceAllInfoDetail();
        }else {
            voiceDeviceAllInfoDetail= tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
        }
        //找到对应的pms文件
        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String filePathAndName = resMap.get("content") +  "/PMS/CameraPMS.xml";
//        String filePathAndName = "D:/testform/PMS/CameraPMS.xml";
        log.info("路径是==="+filePathAndName);

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        //解析xml
        Element rootElement = document.getRootElement();

        Iterator iterator = rootElement.elementIterator();
        Map<String,String> map = new HashMap<>();
        while (iterator.hasNext()) {
            Element stu = (Element) iterator.next();
            List<Attribute> attributes = stu.attributes();

            for (Attribute attribute : attributes) {
                Iterator iterator1 = stu.elementIterator();
                while (iterator1.hasNext()) {
                    Element stuChild = (Element) iterator1.next();
                    map.put(stuChild.getName(), stuChild.getStringValue());
                }
                if (map.containsKey("voiceDeviceName")) {
                    voiceDeviceAllInfoDetail.setVoiceDeviceName(map.get("voiceDeviceName"));
                }
                if (map.containsKey("stdDeviceId")) {
                    voiceDeviceAllInfoDetail.setStdDeviceId(Long.valueOf(map.get("stdDeviceId")));
                }
                if (map.containsKey("upRegionId")) {
                    voiceDeviceAllInfoDetail.setUpRegionId(Long.valueOf(map.get("upRegionId")));
                }
                if (map.containsKey("ftpUrl")) {
                    voiceDeviceAllInfoDetail.setFtpUrl(map.get("ftpUrl"));
                }
                if (map.containsKey("owner")) {
                    voiceDeviceAllInfoDetail.setOwner(map.get("owner"));
                }
                if (map.containsKey("ownerCode")) {
                    voiceDeviceAllInfoDetail.setOwnerCode(map.get("ownerCode"));
                }
                if (map.containsKey("port")) {
                    voiceDeviceAllInfoDetail.setPort(Integer.valueOf(map.get("port")));
                }
                if (map.containsKey("absoluPath")) {
                    voiceDeviceAllInfoDetail.setAbsoluPath(map.get("absoluPath"));
                }
                if (map.containsKey("dbValue")) {
                    voiceDeviceAllInfoDetail.setDbValue(map.get("dbValue"));
                }
                if (map.containsKey("fValue")) {
                    voiceDeviceAllInfoDetail.setfValue(map.get("fValue"));
                }
                if (map.containsKey("channelNum")) {
                    voiceDeviceAllInfoDetail.setChannelNum(map.get("channelNum"));
                }
                if (map.containsKey("pmsId")) {
                    voiceDeviceAllInfoDetail.setPmsId(map.get("pmsId"));
                }

                log.info("voiceDeviceAllInfoDetail==="+voiceDeviceAllInfoDetail);
                if(pmsId != null && pmsId.equals(map.get("pmsId"))){
                    this.update(voiceDeviceAllInfoDetail);
                }else {
                    this.add(voiceDeviceAllInfoDetail);
                }
            }
        }
        return 1;
    }

    public VoiceDevice selectVoiceTree(){
        VoiceDevice voiceDevice =new VoiceDevice();
        voiceDevice.setId("-1");
        voiceDevice.setLabel("声纹设备树");
        voiceDevice.setInfoType("area");
        voiceDevice.setChildren(tVoiceDeviceDao.selectVoiceTree());
        return voiceDevice;
    }

    public List<String> voiceRegisted(){
        return audioDeviceManager.registedDevices();
    }

    public void registerAudioDevice(){
        List<TVoiceDevice> list = tVoiceDeviceDao.select(null,null,null,null,null,null,null,null,null);
        list.forEach(tVoiceDevice -> {
            if(tVoiceDevice.getVoiceCode() != null && !"".equals(tVoiceDevice.getVoiceCode())){
                audioDeviceManager.registerAudioDevice(audioDeviceFactory.newAudioDevice(tVoiceDevice.getVoiceCode()));
            }
        });
    }

}

