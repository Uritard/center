package com.yjh.accessvideo.service;

import com.yjh.accessvideo.common.utils.FtpsUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

@Service
@Data
public class ftpsservice {
    @Value("${manager.server.ftps.flag}")
    private String flag;
    @Value("${manager.server.ftps.ip}")
    private String serverip;
    @Value("${manager.server.ftps.port}")
    private String ftpsPort;
    @Value("${manager.server.ftps.username}")
    private String ftpsUserName;
    @Value("${manager.server.ftps.password}")
    private String ftpsPassWord;
    @Value("${manager.server.ftps.keypw}")
    private String key;
    @Value("${manager.server.ftps.remote.path}")
    private String ftpsRemotePath;

    private Logger log = LoggerFactory.getLogger(ftpsservice.class);

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String arlmtype, String filepath,String remotefilename) {
        try {
            Calendar cal = Calendar.getInstance();
            String year =String.valueOf( cal.get(Calendar.YEAR));
            String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            if ("".equals(arlmtype)) {
                return;
            }

            FtpsUtil.putFile(filepath, remotefilename,
                    serverip, Integer.valueOf(ftpsPort), key, ftpsUserName, ftpsPassWord);
        } catch (Exception e) {
            log.error("上传至ftps错误 " + e);
        }
    }

    public boolean fileExits(String filepath){
        return FtpsUtil.isFTPFileExist(filepath,serverip,Integer.valueOf(ftpsPort),key,ftpsUserName, ftpsPassWord);
    }
}
