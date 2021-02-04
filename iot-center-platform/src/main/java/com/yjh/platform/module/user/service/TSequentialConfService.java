package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TCfgDeviceDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCfgDevice;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import com.yjh.platform.module.user.controller.TSequentialConfController;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.user.dao.TSequentialConfDao;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.entity.TSysParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2021-01-21
*/
@Service
public class TSequentialConfService{

    @Autowired
    private TSequentialConfDao tSequentialConfDao;
    @Autowired
    private TSysParamDao tSysParamDao;
    @Autowired
    private TCfgDeviceDao tCfgDeviceDao;


    private Logger log = LoggerFactory.getLogger(TSequentialConfService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(TSequentialConf tSequentialConf) {
        List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
        if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
            return -1;
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        if(tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId()) != null){
           return tSequentialConfDao.update(tSequentialConf);
        }else {
            return this.tSequentialConfDao.add(tSequentialConf);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.deleteByPrimaryId(cfgDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TSequentialConf tSequentialConf) {
        TSequentialConf old = tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId());
        if(old.getCameraId() != tSequentialConf.getCameraId()){
            List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
            if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
                return -1;
            }
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        return this.tSequentialConfDao.update(tSequentialConf);
    }

    @Transactional(rollbackFor = Exception.class)
    public TSequentialConf selectByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.selectByPrimaryId(cfgDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> select(String cfgDeviceId, String cfgMeteId, Long presetId, String identifyResult) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.select(cfgDeviceId, cfgMeteId, presetId, identifyResult);
        return tSequentialConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> selectByPage(String cfgDeviceName) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.selectByPage(cfgDeviceName);
        return tSequentialConfList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TSequentialConf> list) {
        return this.tSequentialConfDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String cfgDeviceId) {
    List<String> list1= Arrays.asList(cfgDeviceId.split(","));
    return this.tSequentialConfDao.batchDelete(list1);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectForCfgDeviceTree(String cfgDeviceName){
        List<AreaInfo> list = new LinkedList<>();
        //遥信
//        List<AreaInfo> listItem1 = this.tSequentialConfDao.selectForTCfgMete(1,cfgDeviceName);
//        AreaInfo areaInfoItem1 = new AreaInfo();
//        areaInfoItem1.setId(-1L);
//        areaInfoItem1.setUpId(null);
//        areaInfoItem1.setLabel("遥控设备树");
//        areaInfoItem1.setChildren(listItem1);
//        areaInfoItem1.setInfoType("tree");
        //list.add(areaInfoItem1);
        //遥测
//        List<AreaInfo> listItem2 = this.tSequentialConfDao.selectForTCfgMete(2,cfgDeviceName);
//        AreaInfo areaInfoItem2 = new AreaInfo();
//        areaInfoItem2.setId(2L);
//        areaInfoItem2.setUpId(-2L);
//        areaInfoItem2.setLabel("遥测");
//        areaInfoItem2.setChildren(listItem2);
//        areaInfoItem2.setInfoType("meteKind");
        //list.add(areaInfoItem2);
        //遥测
        List<AreaInfo> listItem3 = this.tSequentialConfDao.selectForTCfgMete(3,cfgDeviceName);
        AreaInfo areaInfoItem3 = new AreaInfo();
        areaInfoItem3.setId(3L);
        areaInfoItem3.setUpId(-1L);
        areaInfoItem3.setLabel("遥控");
        areaInfoItem3.setChildren(listItem3);
        areaInfoItem3.setInfoType("meteKind");
        list.add(areaInfoItem3);
        //遥测
//        List<AreaInfo> listItem4 = this.tSequentialConfDao.selectForTCfgMete(4,cfgDeviceName);
//        AreaInfo areaInfoItem4 = new AreaInfo();
//        areaInfoItem2.setId(4L);
//        areaInfoItem2.setUpId(-4L);
//        areaInfoItem2.setLabel("遥调");
//        areaInfoItem2.setChildren(listItem4);
//        areaInfoItem2.setInfoType("meteKind");
        //list.add(areaInfoItem4);

        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequential(String meteId) throws Exception{
        {
            Map<String,String> map = this.sequentialInfo(meteId).get(0);
            Map<String, Object> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "newSequential");
            jasonMaps2.put("cfgDeviceId", meteId);
            jasonMaps2.put("sort", map.get("sort"));
            jasonMaps2.put("state", map.get("state"));
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            WebSocketServer.sendMsg(json);
            //结果
//            {"type": "newSequentialResult",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合",
//                    "identifyResult": "合"
//            }
//            顺控
//            {"type": "newSequential",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合"
//            }

            Thread.sleep(20000);
            //todo 生成一个巡视任务

            //todo 发给算法进行分析

            //Map<String,String> mapResult = this.sequentialInfo(meteId).get(0);
            TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
            //String devicePath = tSysParam.getContent()+"/"+"sequential.txt";
            String devicePath = "D:/code/qhTest/sequential.txt";
            try{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //File txt=new File("D:/code/qhTest/sequential.txt");
                File txt=new File(devicePath);

                if(txt.exists()){
                    txt.delete();
                }
                if (!txt.exists()) {
                    txt.createNewFile();
                }
                FileWriter fw = new FileWriter(txt, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间）!>\r\n");
                bw.write("<DeviceInfo::设备状态>\r\n");
                bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
                bw.write("#1\t"+1+"\t"+meteId+"\t"+map.get("deviceName")+"\t"+map.get("state")+"\t"+simpleDateFormat.format(new Date())+"\r\n");
                bw.write("</DeviceInfo::设备状态>\r\n");
                bw.flush();
                bw.close();
                fw.close();
                //将生成的顺控确认文件发送给主辅监控系统
                Map<String,List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list",list);
                Constant.otherServer(mapForSend,Constant.UDP_SEND);
            }catch (Exception e){log.info("生成顺控确认文件失败"+e.getMessage());}

            Map<String, Object> jasonMapsResult = new HashMap<>();
            jasonMapsResult.put("type", "newSequentialResult");
            jasonMapsResult.put("cfgDeviceId", meteId);
            jasonMapsResult.put("sort", map.get("sort"));
            jasonMapsResult.put("state", map.get("state"));
            jasonMapsResult.put("identifyResult", map.get("state"));
            String jsonResult = JSON.toJSONString(jasonMapsResult);
            log.info("发送给前端的消息：" + jsonResult);
            WebSocketServer.sendMsg(jsonResult);

            //todo 生成顺控文件
        }
       return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> sequentialInfo(String cfgDeviceId){
        //todo 写入识别结果
         return tSequentialConfDao.selectForSequenceInfo(cfgDeviceId);
    }
    @Transactional(rollbackFor = Exception.class)
    public String unionTask(String cfgDeviceId,String order){

        TCfgDevice tCfgDevice = tCfgDeviceDao.selectByPrimaryId(cfgDeviceId);
        TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
        String devicePath = tSysParam.getContent()+"/"+"unionTask.txt";
        //String devicePath = "D:/code/qhTest/unionTask.txt";
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //File txt=new File("D:/code/qhTest/sequential.txt");
            File txt=new File(devicePath);

            if(txt.exists()){
                txt.delete();
            }
            if (!txt.exists()) {
                txt.createNewFile();
            }
            FileWriter fw = new FileWriter(txt, true);
            //BufferedWriter bw = new BufferedWriter(fw,"UTF-8");
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(txt), "UTF-8"));

            bw.write("<!Entity=反向联动请求\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::控制状态信息>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t类型\t联动指令\r\n");
            bw.write("#1\t"+1+"\t"+cfgDeviceId+"\t"+tCfgDevice.getDeviceName()+"\t"+"遥控"+"\t"+order+"\r\n");
            bw.write("</DeviceInfo::设备资源信息>\r\n");
            bw.flush();
            bw.close();
            fw.close();
            //将生成的反向联动文件发送给主辅监控系统
            Map<String,List<String>> mapForSend = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add(devicePath);
            mapForSend.put("list",list);
            Constant.otherServer(mapForSend,Constant.UDP_SEND);
        }catch (IOException e){log.error("生成顺控确认文件失败"+e);}
         catch (Exception e) { log.error("发送顺控确认文件失败"+e); }

        return "ok";
    }



}

