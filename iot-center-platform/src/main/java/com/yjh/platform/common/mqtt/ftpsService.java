package com.yjh.platform.common.mqtt;


import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.threadpool.TaskExecutePool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Data
public class ftpsService {
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

    private Logger log = LoggerFactory.getLogger(ftpsService.class);

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String alarmType, String filePath,String remoteFileName) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if ("".equals(alarmType)) {
                        return;
                    }
                    FtpsUtil.putFile(filePath, remoteFileName, serverip, Integer.valueOf(ftpsPort), key, ftpsUserName, ftpsPassWord);
                } catch (Exception e) {
                    log.error("上传至ftps错误: " + e);
                }
            }
        };
        TaskExecutePool.getInstance().execute(runnable);
    }

    public boolean fileExits(String filepath){
        return FtpsUtil.isFTPFileExist(filepath,serverip,Integer.valueOf(ftpsPort),key,ftpsUserName, ftpsPassWord);
    }

}
