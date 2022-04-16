package com.yjh.accessvideo.service;

import com.yjh.accessvideo.common.utils.FtpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

@Service
public class ftpsservice {
    private String serverUrl;
    @Value("${netty.server.ftps.port}")
    private String ftpsPort;
    @Value("${netty.server.ftps.username}")
    private String ftpsUserName;
    @Value("${netty.server.ftps.password}")
    private String ftpsPassWord;
    @Value("${netty.server.ftps.keypw}")
    private String key;
    @Value("${netty.server.ftps.local.path}")
    private String ftpsLocalPath;

    private Logger log = LoggerFactory.getLogger(ftpsservice.class);

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String arlmtype, String filename) {
        try {
            Calendar cal = Calendar.getInstance();
            String year =String.valueOf( cal.get(Calendar.YEAR));
            String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            if ("".equals(arlmtype)) {
                return;
            }
            String ftppath=ftpsLocalPath + "/" +arlmtype+"/"+year+month;
            FtpsUtil.putFile(ftppath, filename,
                    serverUrl, Integer.valueOf(ftpsPort), key, ftpsUserName, ftpsPassWord);
        } catch (Exception e) {
            log.error("上传至ftps错误 " + e);
        }
    }
}
