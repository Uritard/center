package com.yjh.platform.module.device.service;

import com.yjh.platform.common.utils.mp3.VoiceAnalyseUtil;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TCameraInfo;
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
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private Logger log = LoggerFactory.getLogger(TVoiceDeviceService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(VoiceDeviceAllInfoDetail tVoiceDevice) {
        List<TStdDevice> tStdDevice = tStdDeviceDao.selectByPrimaryId(tVoiceDevice.getStdDeviceId());
        if(tStdDevice.size()!=0){
            tVoiceDevice.setDeviceType(tStdDevice.get(0).getDeviceType().toString());
            //tVoiceDevice.setStationId(tStdDevice.getS)
        }else {
            return -1;
        }
        this.tVoiceDeviceDao.addConf(tVoiceDevice);
        return this.tVoiceDeviceDao.add(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long voiceDeviceId) {
        VoiceDeviceAllInfoDetail voiceDeviceInfoDetail = this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
        this.tVoiceDeviceDao.deleteConf(voiceDeviceInfoDetail.getConfigId());
        return this.tVoiceDeviceDao.deleteByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(VoiceDeviceAllInfoDetail tVoiceDevice) {
        List<TStdDevice> tStdDevice = tStdDeviceDao.selectByPrimaryId(tVoiceDevice.getStdDeviceId());
        if(tStdDevice.size() != 0){
            tVoiceDevice.setDeviceType(tStdDevice.get(0).getDeviceType().toString());
            //tVoiceDevice.setStationId(tStdDevice.getS)
        }else {
            return -1;
        }
        this.tVoiceDeviceDao.updateConf(tVoiceDevice);
        return this.tVoiceDeviceDao.update(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public VoiceDeviceAllInfoDetail selectByPrimaryId(Long voiceDeviceId) {
        return this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> select(Long voiceDeviceId, String voiceDeviceName, Long stdDeviceId, String deviceType, Long configId,Long upRegionId) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId, upRegionId);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<VoiceDeviceAllInfoDetail> selectByPage(String voiceDeviceName,String deviceType,Long upRegionId) {
        List<Long> list = tStdRegionDao.selectDownId(upRegionId);
        if(upRegionId != null){
            list.add(upRegionId);
        }
        List<VoiceDeviceAllInfoDetail> tVoiceDeviceList = tVoiceDeviceDao.selectByPage(voiceDeviceName,deviceType,list,upRegionId);
        return tVoiceDeviceList;
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
//        String realPath = "D:/code/qhTest";
//        String absPath  = "D:/code/qhTest";
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
                    List<VoiceDevice> channelList = new ArrayList<>();
                    if("device".equals(areaInfoTem.getInfoType())){
                        //todo 记得加上日期这一层级日期
                        boolean flag = false;
                        //File file = new File("D:/code/qhTest"+"/"+areaInfoTem.getId());
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
                                    //查询此文件夹下的所有文件
                                    List<VoiceDevice> dateList = new ArrayList<>();
                                    File fileForDate = new File(absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName());
                                    File[] voiceDateList = fileForDate.listFiles();
                                    if(voiceDateList != null && voiceDateList.length>0) {
                                        for (int j = 0; j < voiceDateList.length; j++) {
                                            if (voiceDateList[j].isDirectory()) {//日期文件夹
                                                VoiceDevice date = new VoiceDevice();
                                                date.setUpId(channel.getId());
                                                date.setUpName(channel.getLabel());
                                                date.setLabel(voiceDateList[j].getName());
                                                date.setId(voiceDateList[j].getName());
                                                //date.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName());
                                                date.setInfoType("date");
                                                //查询此文件夹下的所有文件
                                                List<VoiceDevice> fileList = new ArrayList<>();
                                                String path = absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName();
//                                                File fileForFile = new File(absPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName());
//                                                File[] voiceFileList = fileForFile.listFiles();
                                                List<File> voiceFileList = getFileSort(path);
                                                if(voiceFileList != null && voiceFileList.size()>0) {
                                                    for (int k = 0; k < voiceFileList.size(); k++) {
                                                        if (voiceFileList.get(k).isFile() && voiceFileList.get(k).getName().contains(".wav")) {//音频文件
                                                            VoiceDevice voiceFile = new VoiceDevice();
                                                            voiceFile.setUpId(date.getId());
                                                            voiceFile.setUpName(date.getLabel());
                                                            voiceFile.setLabel(voiceFileList.get(k).getName());
                                                            voiceFile.setFilePath(realPath+"/"+areaInfoTem.getId()+"/"+channelFileList[i].getName()+"/"+voiceDateList[j].getName()+"/"+voiceFileList.get(k).getName());
                                                            voiceFile.setInfoType("file");
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
        //String path = "D:/code/qhTest";
        Long voiceDeviceId = null;
        String absPath = tSysParamDao.selectByParamType("absVoicePath").getContent();
        String realPath = tSysParamDao.selectByParamType("relativeVoicePath").getContent();
//        String realPath = "D:/code/qhTest";
//        String absPath  = "D:/code/qhTest";
        String[] getId = voicePath.replaceAll(realPath,"").split("/");
        if(getId != null && getId.length>2){
            if("".equals(getId[0])){
                voiceDeviceId = Long.valueOf(getId[2]);
            }else {
                voiceDeviceId = Long.valueOf(getId[1]);
            }
        }
        voicePath = voicePath.replaceAll(realPath,absPath);
        MultimediaObject multimediaObject = new MultimediaObject(new File(voicePath));
        MultimediaInfo info = multimediaObject.getInfo();
        Long playTime = info.getDuration();
        VoiceDeviceInfoDetail voiceDeviceInfoDetail = tVoiceDeviceDao.selectVoiceInfo(voiceDeviceId);
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
}

