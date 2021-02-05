package com.yjh.accessvqd.module.diagnose.service;


import com.google.gson.internal.$Gson$Preconditions;
import com.yjh.accessvqd.commons.logs.Logs;
import com.yjh.accessvqd.commons.utils.http.HttpClientUtils;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.ChannelsXML;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.DataServerXML;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.ResponseXML;
import com.yjh.accessvqd.module.diagnose.entity.Channel;
import com.yjh.accessvqd.module.diagnose.entity.DataServer;
import com.yjh.accessvqd.module.diagnose.entity.TestList;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author czh
 * @since 2021-01-14
 */
@Service
public class ChannelService {
    @Value("${diagnose.url}"+"/PSIA/Custom/SelfExt/AS/VQDDiagnose/dataServers")
    private String diagnoseURL;

    @Value("${diagnose.url}"+"/PSIA/Custom/SelfExt/AS/VQDDiagnose/channels")
    private String channelURl;

    private Logger log= LoggerFactory.getLogger(ChannelService.class);


    @Transactional(rollbackFor = Exception.class)
    public String userPwdEncrypt(String passWord){
        String key = "ivms6@hikvision$";
        String iv="8807599889957088";
        TestList test = new TestList();
        byte[] js = test.encrypt(passWord.getBytes(),key.getBytes(),iv.getBytes());
        return Base64.getEncoder().encodeToString(js);
    }

    @Logs(title = "数据服务器信息配置-查询",code = "accessvqd/diagnose")
    @Transactional(rollbackFor = Exception.class)
    public String dataServerConfig(){
        String server=null;
        try{
            //数据服务器配置信息查询
            String configResult= HttpClientUtils.getInstance().getUrl(diagnoseURL,null);
            server=configResult;
        }catch (Exception e){
            log.error("调用失败"+e);
        }

        return server;
    }

    @Logs(title = "数据服务器配置-新增",code ="accessvqd/diagnose")
    @Transactional(rollbackFor = Exception.class)
    public String  addDataServer(DataServer dataServer){
        String status="failed";
        try {
            //数据服务器新增
            String flag=HttpClientUtils.getInstance().postUrl(diagnoseURL, DataServerXML.generateDataServerXML(dataServer));
            if(flag.contains("0"))
                status="success";
        }catch (Exception e){
            log.error("新增失败"+e);
        }
        return status;
    }

    @Logs(title = "数据服务器配置-修改",code = "accessvqd/diagnose")
    @Transactional(rollbackFor = Exception.class)
    public String updateDataServer(DataServer dataServer){
        String status="failed";
        try {
            String flag=HttpClientUtils.getInstance().putUrl(diagnoseURL+"/"+dataServer.getServerIp(), DataServerXML.generateDataServerXML(dataServer));
            if(flag.contains("1"))
                status="success";
        }catch (Exception e){
            log.error("更新失败"+e);
        }
        return status;
    }

    @Logs(title = "数据服务器配置-删除", code = "accessvqd/diagnose")
    @Transactional(rollbackFor = Exception.class)
    public String deleteDateServer(String serverId){
        String status="failed";
        try {
            String flag=HttpClientUtils.getInstance().deleteUrl(diagnoseURL+"/"+serverId,null);
            if(flag.contains("0"))
                status="success";
        }catch (Exception e){
            log.error("删除失败"+e);
        }
        return status;
    }

    @Logs(title = "监测点列表查询")
    @Transactional(rollbackFor = Exception.class)
    public List<Channel> channelInfo(){
        List<Channel> channelList=new ArrayList<>();
        try{
            String channels=HttpClientUtils.getInstance().getUrl(channelURl,null);
            channelList=ChannelsXML.unPackingXMlToList(channels);
        }catch (Exception e){
            log.error("监测点查询失败"+e);
        }
        return channelList;
    }

    @Logs(title = "单个监测点查询")
    @Transactional(rollbackFor = Exception.class)
    public Channel getChannel(String channelId){
        Channel channel=new Channel();
        try {
            String channels=HttpClientUtils.getInstance().getUrl(channelURl+"/"+channelId,null);
            channel=ChannelsXML.unPackingXMl(channels);
        }catch (Exception e){
            log.error("查询失败"+e);
        }
        return channel;
    }

    @Logs(title = "新增/修改监测点")
    @Transactional(rollbackFor = Exception.class)
    public String addChannel(Channel channel){
        String status=null;
        try {
            log.info("channel:----------"+channel);
            channel.setUserPwd(userPwdEncrypt(channel.getUserPwd()));
            String result=HttpClientUtils.getInstance().putUrl(channelURl+"/"+channel.getId(), ChannelsXML.generateChannelXML(channel));
            status= ResponseXML.unPackingXMl(result);
        }catch (Exception e){
            log.error("新增失败"+e);
        }
        return status;
    }

    @Logs(title = "删除监测点")
    @Transactional(rollbackFor = Exception.class)
    public String deleteChannel(String channelId){
        String status=null;
        try {
            String result=HttpClientUtils.getInstance().deleteUrl(channelURl+"/"+channelId,null);
            status= ResponseXML.unPackingXMl(result);
        }catch (Exception e){
            log.error("删除失败"+e);
        }
        return status;
    }

}
