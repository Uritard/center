package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.thread.TaskExecutePool;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accesstcp.common.Constant.Packet;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private byte[] data;
    private TCPClientHandler tcpClientHandler;
    private RedisTemplate redisTemplate;

    public DataDealThread(byte[] data,TCPClientHandler tcpClientHandler,RedisTemplate redisTemplate) {
        this.data =data;
        this.redisTemplate = redisTemplate;
        this.tcpClientHandler = tcpClientHandler;
    }

    @Override
    public void run() {
        StringBuilder dataString = new StringBuilder();
        for (byte byteitem : data) {
            dataString.append(String.format("%02x ", byteitem));
        }
        System.out.println("我在处理数据了"+dataString);
        //处理毡包问题
        String zzbds ="^.*<?xml.*";
        if (!Packet.matches(zzbds)){
            Packet = "";
        }

        String temporaryBody = Packet + dataString ;//临时
        String temporaryBody2 = temporaryBody.replace("\"UTF-8\"","\'UTF-8\'");//临时
        String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body
        try{
            handlingMethod(data,finalBody);
        }catch (Exception e){}


    }
    //拆包 解决毡包
    private void handlingMethod(byte[] bytes,String parameter)throws Exception {

        String hua = null;

        if (parameter.contains("开始") || parameter.contains("结束")) {
            hua = parameter;
        } else {
            String bian = parameter.replace("<?xml version='1.0' encoding='UTF-8'?>", "开始<?xml version='1.0' encoding='UTF-8'?>");
            //todo 记得改
            hua = bian.replace("</Robot>", "</Robot>结束");
        }
        Pattern pattern1 = Pattern.compile("(\\<\\?xml version='1.0' encoding='UTF-8'?[^>])([\\s\\S]*?)(</Robot>)");
        Matcher matcher1 = pattern1.matcher(hua);
        String wanZheng = null;
        String shengYu = null;
        if (matcher1.find()) {
            wanZheng = matcher1.group();
            log.info("匹配到一个完整包的内容=" + wanZheng);
            stringToXml(bytes, wanZheng);
            shengYu = parameter.replace(wanZheng, "");
            handlingMethod(bytes, shengYu);
        } else {
            Packet = parameter;
//                log.info("不足一个完整的包：" + Packet);
        }

    }
    private void stringToXml(byte[] bytes,String xmlContext)throws Exception{

        Document document = DocumentHelper.parseText(xmlContext);//String转XML
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
        log.info("解析出来的xml是：" + xmlRes);
        byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
        byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
        Constant.receiveSessionId = sendSessionId;
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
        if (xmlRes.getSendCode() == null) {
            log.info("连接可能断了，等待重连.....");
        } else {
            doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
        }

    }
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {
        //解析的xml文件
        if ("251".equals(xmlBaseModel.getType())) {
            if ("4".equals(xmlBaseModel.getCommand())) {//响应注册
                if ("100".equals(xmlBaseModel.getCommand())) {
                    //需要重发注册消息
                    tcpClientHandler.sendRegister();
                } else if ("200".equals(xmlBaseModel.getCommand())) {
                    //服务端响应 我方开启心跳

                    List<Map<String, Object>> items = xmlBaseModel.getItems();
                    if (items == null || items.size() == 0) {
                        return;
                    }
                    for (Map<String, Object> item : items) {

                    }
                    Constant.paramMap.put("heart_beat_interval", "");//心跳间隔
                    Constant.paramMap.put("patroldevice_run_interval", "");//巡视设备运行数据间隔间隔
                    Constant.paramMap.put("weather_interval", "");//微气象数据间隔
                    //将数据放入redis 做个保存
                    redisTemplate.opsForHash().putAll("upSystemParameter",Constant.paramMap);
                    //todo 记得做 启动响应线程处理响应业务

                    //心跳线程发心跳
                    HeartBeatThead heartBeatThead = new HeartBeatThead(tcpClientHandler, true);
                    TaskExecutePool.getInstance().execute(heartBeatThead);

                } else {
                    return;
                }

            }

            if ("3".equals(xmlBaseModel.getCommand())) {//响应心跳
            }
        }
    }
}
