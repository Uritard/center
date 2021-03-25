package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.dao.SendToUpSystemDao;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.server.TCPClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Service
public class SendToUpSystemServices {

    @Value("${netty.server.port}")
    private int port;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${spring.union.stationCode}")
    private String stationCode;

    @Autowired
    private SendToUpSystemDao sendToUpSystemDao;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public int sendResponse(String type,String command,String code,List<Map<String,Object>> items){
        byte[] bytes = new byte[]{};
        long sendSessionId = Constant.sendSessionId;
        TCPClientHandler tcpClientHandler = TCPClientHandler.getTCPClientHandlerHashMap().get(port);
        if(tcpClientHandler != null){
            sendSessionId = sendSessionId + 1L;//请求报文每次累加1
            Constant.sendSessionId = sendSessionId;//刷新sendSessionId
        }else {
            Constant.sendSessionId = 0L;//刷新sendSessionId
            return -1;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Client02")
                .setReceiveCode("Server02")
                .setType(type)
                .setCommand(command)
                .setCode(code)
                .setItems(items);
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        bytes = PlatformPacketUtil.createPacket(sendSessionId,Constant.receiveSessionId,true,xml);
        tcpClientHandler.send(bytes);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> creatFile(){
        try{
            Map<String,Object> map = new HashMap<>();
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String path = mapForPath.get("content")+"/"+stationCode+"/Model";

            List<Map<String,Object>> list = sendToUpSystemDao.selectDeviceModel();
            map.put("device_file_path",CreateModeXMLUtil.createXmlFile(list,path,"device_model.xml","Device_Model"));
            list = sendToUpSystemDao.selectRobotInfo();
            map.put("robot_file_path",CreateModeXMLUtil.createXmlFile(list,path,"robot_model.xml","Robot_Model"));
            list = sendToUpSystemDao.selectTaskInfo();
            //CronExpression expression;
            for (Map<String,Object> item:list) {
                String taskId = item.get("task_code").toString();
                List<Long> instanceIdList = sendToUpSystemDao.selectInstanceId(taskId);
                item.put("device_list",instanceIdList.toString().replaceFirst("\\[","").replace("]","").replace(" ",""));
                if(!"".equals(item.get("time"))){
                    //计算 todo 内容不完整
                    String time = item.get("time").toString();
                    //SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
                    //String s = Pattern.compile("[^0-9]").matcher(str).replaceAll("");

//                    item.put("cycle_month", CornUtil.translateToChinese(time,1));
//                    item.put("cycle_week",CornUtil.translateToChinese(time,2));
//                    item.put("cycle_execute_time",CornUtil.translateToChinese(time,4));

                    item.put("cycle_start_time",simpleDateFormat.format(new Date()));
                    item.put("cycle_end_time","2025-01-01 00:00:00");
                    item.put("interval_number","1");
                    item.put("interval_type","2");
                    item.put("interval_execute_time","00:00:00");
                    item.put("interval_start_time",simpleDateFormat.format(new Date()));
                    item.put("interval_end_time","2025-01-01 00:00:00");
                    if("1".equals(map.get("isenable"))){
                        item.put("invalid_start_time",simpleDateFormat.format(new Date()));
                        item.put("invalid_end_time",simpleDateFormat.format(new Date()));
                    }


                }
            }
            map.put("task_file_path",CreateModeXMLUtil.createXmlFile(list,path,"task_model.xml","Task_Model"));
            return map;
        }catch (Exception e) {
        e.printStackTrace();
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public int sendXML(XMLBaseModel xmlBaseModel){
        return this.sendResponse(xmlBaseModel.getType(),xmlBaseModel.getCommand(),xmlBaseModel.getCode(),xmlBaseModel.getItems());
    }
}
