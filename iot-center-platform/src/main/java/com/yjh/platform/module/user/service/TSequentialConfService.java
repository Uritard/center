package com.yjh.platform.module.user.service;

import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.HttpClientUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

import javax.management.ObjectName;

import static com.yjh.platform.common.Constant.redisTemplate;

/**
* @author lqh
* @since 2021-01-21
*/
@Service
public class TSequentialConfService{

    @Value("${sequential.videocfmresult}")
    private String videocfmresultFile;

    @Value("${sequential.returnlinkage}")
    private String returnlinkageFile;
    @Value("${sequential.cameraCapture}")
    private String cameraCapture;
    @Value("${sequential.picRec}")
    private String picRec;

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
        if(!old.getCameraId().equals(tSequentialConf.getCameraId())){
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
        List<AreaInfo> listItem1 = this.tSequentialConfDao.selectForTCfgMete(1,cfgDeviceName);
        AreaInfo areaInfoItem1 = new AreaInfo();
        areaInfoItem1.setId(-1L);
        areaInfoItem1.setUpId(null);
        areaInfoItem1.setLabel("遥信");
        areaInfoItem1.setChildren(listItem1);
        areaInfoItem1.setInfoType("meteKind");
        list.add(areaInfoItem1);
        //遥测
        List<AreaInfo> listItem2 = this.tSequentialConfDao.selectForTCfgMete(2,cfgDeviceName);
        AreaInfo areaInfoItem2 = new AreaInfo();
        areaInfoItem2.setId(2L);
        areaInfoItem2.setUpId(-2L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);
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
        List<AreaInfo> listItem4 = this.tSequentialConfDao.selectForTCfgMete(4,cfgDeviceName);
        AreaInfo areaInfoItem4 = new AreaInfo();
        areaInfoItem4.setId(4L);
        areaInfoItem4.setUpId(-4L);
        areaInfoItem4.setLabel("遥调");
        areaInfoItem4.setChildren(listItem4);
        areaInfoItem4.setInfoType("meteKind");
        list.add(areaInfoItem4);

        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequential(String meteId) throws Exception{
        {
            Map<String,Object> map = this.sequentialInfo(meteId).get(0);
            Map<String, String> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "newSequential");
            jasonMaps2.put("cfgDeviceId", meteId);
            jasonMaps2.put("sort", /*String.valueOf(Double.valueOf(*/map.get("sort").toString()/*).intValue())*/);
            Constant.sequentialState.put("state",((Long)map.get("sort")).intValue());
            Constant.sequentialState.put("cfgDeviceId",meteId);
            Constant.sequentialState.put("meteResult",map.get("state"));
            jasonMaps2.put("state", "进行中");
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMaps2);
            }catch (Exception e){
                System.out.println("发送websocket出错");
            }

            if(meteId != null && !"".equals(meteId)){
                TSequentialConf sequentialConf = this.selectByPrimaryId(meteId);
                if(sequentialConf != null && sequentialConf.getPresetId() != null){
                    this.update(sequentialConf);
                }
            }
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

//            Thread.sleep(20000);
//            //todo 生成一个巡视任务
//
//            //todo 发给算法进行分析
//
//            try{
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
//                //Map<String,String> mapResult = this.sequentialInfo(meteId).get(0);
//                TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
//                String devicePath = tSysParam.getContent()+"/"+videocfmresultFile.replace(".",simpleDateFormat2.format(new Date())+".");
//
//                File txt=new File(devicePath);
//                if(txt.exists()){
//                    txt.delete();
//                }
//                if (!txt.exists()) {
//                    txt.createNewFile();
//                }
////                FileWriter fw = new FileWriter(txt, true);
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
//                        new FileOutputStream(txt,true), "UTF-8"));
//                bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间）!>\r\n");
//                bw.write("<DeviceInfo::设备状态>\r\n");
//                bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
//                bw.write("#1\t"+1+"\t"+meteId+"\t"+map.get("deviceName")+"\t"+map.get("state")+"\t"+simpleDateFormat.format(new Date())+"\r\n");
//                bw.write("</DeviceInfo::设备状态>\r\n");
//                bw.flush();
//                bw.close();
////                fw.close();
//                //将生成的顺控确认文件发送给主辅监控系统
//                Map<String,List<String>> mapForSend = new HashMap<>();
//                List<String> list = new ArrayList<>();
//                list.add(devicePath);
//                mapForSend.put("list",list);
//                Constant.otherServer(mapForSend,Constant.UDP_SEND);
//            }catch (Exception e){log.info("生成顺控确认文件失败"+e.getMessage());}
//
//            Map<String, String> jasonMapsResult = new HashMap<>();
//            jasonMapsResult.put("type", "newSequentialResult");
//            jasonMapsResult.put("cfgDeviceId", meteId);
//            jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
//            jasonMapsResult.put("state", map.get("state").toString());
//            jasonMapsResult.put("identifyResult", map.get("state").toString());
//            String jsonResult = JSON.toJSONString(jasonMapsResult);
//            log.info("发送给前端的消息：" + jsonResult);
//            try{
//                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
//            }catch (Exception e){
//                System.out.println("发送websocket出错");
//            }
//
//            List<String> listSort = tSequentialConfDao.selectLastStep();
//            if(listSort.get(listSort.size()-1).equals(meteId) ){
//                //这是最后一个步骤
//                TSysParam time = tSysParamDao.selectByParamType("cleanTime");
//                Thread.sleep(Integer.valueOf(time.getContent())*1000);
//                Constant.sequentialState.put("state",-1);
//                Constant.sequentialState.put("cfgDeviceId","");
//            }
            //todo 生成顺控文件
        }
       return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRec(String meteId) throws Exception{
        {

            Map<String,Object> map = tSequentialConfDao.selectForSequenceInfoByMeteId(meteId).get(0);
            Map<String , Object> param = new HashMap<>();
            //收到变位信号抓图
            try{
                String services = HttpClientUtils.getInstance().getUrl(cameraCapture+ "?cameraId=" +  map.get("cameraId").toString(), null);
                JSONObject jsonObject =JSONObject.parseObject(services);
                Map<String,Object> re= (Map<String,Object>) jsonObject.get("data");
                if(!Objects.isNull(re.get("absPath"))){
                    param.put("picPath",re.get("absPath").toString());
                }else{
                    throw new BusinessException("一键顺控-变位信号-抓图地址为空");
                }
            }catch (Exception e){
                log.error("一键顺控-变位信号-抓图失败",e.getMessage());
            }
            //todo 发给算法进行分析
            try{
                param.put("analyseType",6);
                param.put("instanceId",map.get("cfgDeviceId"));
                param.put("isAi",1);//模板图片路径
                Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                String picModelPath = (String) mapForPicModelPath.get("content");

                param.put("picModelPath",picModelPath+"/"+map.get("presetId"));
                param.put("taskId",UUID.randomUUID()+"#yjsk#meteId="+map.get("cfgDeviceId"));
                List<Map<String,Object>> analysis = new ArrayList<>();
                analysis.add(param);
                log.info("调用video算法识别接口param={},url={}",JSON.toJSONString(analysis),picRec);
                Map<String,List<Map<String,Object>>> analysisList = new HashMap<>();
                analysisList.put("list",analysis);
                ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
                if (null != serviceRestTemplate) {
                    serviceRestTemplate.postForObject(picRec, analysisList, String.class);
                }
//
//                String services = HttpClientUtils.getInstance().postUrl(picRec, JSON.toJSONString(analysis));
//                JSONObject jsonObject =JSONObject.parseObject(services);
//                if(!Objects.isNull(jsonObject.get("code"))&&jsonObject.get("code").toString().equals("200")){
//                    log.info("一键顺控-变位信号-发送至算法识别主机成功");
//                }else{
//                    throw new BusinessException("一键顺控-变位信号-发送至算法识别主机失败"+jsonObject.toJSONString());
//                }
            }catch (Exception e){
                log.error("一键顺控-变位信号-调用算法识别主机失败:{}",e);
            }
        }
        return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public String sequentialRecBack(Map<String,String> recBack) throws Exception{
        {
            Map<String,Object> map = this.sequentialInfo(recBack.get("meteId")).get(0);
            Map<String , Object> param = new HashMap<>();
            try{
                if(!Objects.isNull(recBack.get("code"))&&recBack.get("code").equals("200")){
                    if(!Objects.isNull(recBack.get("desc"))){
                        param.put("resultValue",recBack.get("desc"));
                    }else{
                        Map<String, String> jasonMapsResult = new HashMap<>();
                        jasonMapsResult.put("type", "newSequentialResult");
                        jasonMapsResult.put("cfgDeviceId", recBack.get("meteId"));
                        jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
                        jasonMapsResult.put("state", "算法识别失败");
                        jasonMapsResult.put("identifyResult", "算法识别失败");
                        String jsonResult = JSON.toJSONString(jasonMapsResult);
                        log.info("发送给前端的消息：" + jsonResult);
                        try{
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
                        }catch (Exception e){
                            log.error("发送websocket出错",e.getMessage());
                        }
                        throw new BusinessException("一键顺控-变位信号-算法识别错误" + JSON.toJSONString(recBack));
                    }
                }else{
                    Map<String, String> jasonMapsResult = new HashMap<>();
                    jasonMapsResult.put("type", "newSequentialResult");
                    jasonMapsResult.put("cfgDeviceId", recBack.get("meteId"));
                    jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
                    jasonMapsResult.put("state", "算法识别失败");
                    jasonMapsResult.put("identifyResult", "算法识别失败");
                    String jsonResult = JSON.toJSONString(jasonMapsResult);
                    log.info("发送给前端的消息：" + jsonResult);
                    try{
                        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
                    }catch (Exception e){
                        log.error("发送websocket出错",e.getMessage());
                    }
                    throw new BusinessException("一键顺控-变位信号-识别主机返回结果处理失败");
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");
                TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
                String devicePath = tSysParam.getContent()+"/"+videocfmresultFile.replace("{{date}}",simpleDateFormat2.format(new Date()));

                File txt=new File(devicePath);
                if(txt.exists()){
                    txt.delete();
                }
                if (!txt.exists()) {
                    txt.createNewFile();
                }
//                FileWriter fw = new FileWriter(txt, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(txt,true), "UTF-8"));
                bw.write("<!Entity=设备状态请求结果\tver='V1.0'\ttime='"+simpleDateFormat.format(new Date())+"'(文件最新时间）!>\r\n");
                bw.write("<DeviceInfo::设备状态>\r\n");
                bw.write("@序号\t站序号\t监控索引号\t设备名称\t设备状态\t事件时标\r\n");
                bw.write("#1\t"+1+"\t"+recBack.get("meteId")+"\t"+map.get("deviceName")+"\t"+param.get("resultValue")+"\t"+simpleDateFormat.format(new Date())+"\r\n");
                bw.write("</DeviceInfo::设备状态>\r\n");
                bw.flush();
                bw.close();
//                fw.close();
                //将生成的顺控确认文件发送给主辅监控系统
                Map<String,List<String>> mapForSend = new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(devicePath);
                mapForSend.put("list",list);
                Constant.otherServer(mapForSend,Constant.UDP_SEND);
            }catch (Exception e){log.info("生成顺控确认文件失败"+e.getMessage());}

            Map<String, String> jasonMapsResult = new HashMap<>();
            jasonMapsResult.put("type", "newSequentialResult");
            jasonMapsResult.put("cfgDeviceId", recBack.get("meteId"));
            jasonMapsResult.put("sort", String.valueOf(Double.valueOf(map.get("sort").toString()).intValue()));
            jasonMapsResult.put("state", param.get("resultValue").toString());
            jasonMapsResult.put("identifyResult", param.get("resultValue").toString());
            String jsonResult = JSON.toJSONString(jasonMapsResult);
            log.info("发送给前端的消息：" + jsonResult);
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapsResult);
            }catch (Exception e){
                log.error("发送websocket出错",e.getMessage());
            }

            List<String> listSort = tSequentialConfDao.selectLastStep();
            if(listSort.get(listSort.size()-1).equals(map.get("meteId")) ){
                //这是最后一个步骤
                TSysParam time = tSysParamDao.selectByParamType("cleanTime");
                Thread.sleep(Integer.valueOf(time.getContent())*1000);
                Constant.sequentialState.put("state",-1);
                Constant.sequentialState.put("cfgDeviceId","");
            }
            //todo 生成顺控文件
        }
        return "ok";
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> sequentialInfo(String cfgDeviceId){
        //todo 写入识别结果
        return tSequentialConfDao.selectForSequenceInfo(cfgDeviceId);
    }
    @Transactional(rollbackFor = Exception.class)
    public String unionTask(String cfgDeviceId,String order){

        try{
            TCfgDevice tCfgDevice = tCfgDeviceDao.selectByPrimaryId(cfgDeviceId);
            TSysParam tSysParam = tSysParamDao.selectByParamType("unionDeviceInfoPath");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("_yyyyMMdd_HHmmss");

            String devicePath = tSysParam.getContent()+"/"+returnlinkageFile.replace("{{date}}",simpleDateFormat2.format(new Date()));
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

