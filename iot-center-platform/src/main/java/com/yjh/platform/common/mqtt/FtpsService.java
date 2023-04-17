package com.yjh.platform.common.mqtt;

import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Data
public class FtpsService {

    @Autowired
    private ApplicationProperties applicationProperties;

    private Logger log = LoggerFactory.getLogger(FtpsService.class);

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String alarmType, String filePath,String remoteFileName) {
        try {
            if ("".equals(alarmType)) {
                return;
            }
            FtpsUtil.putFile(filePath, remoteFileName, applicationProperties.getManagerSystemFtps().getIp()
                    , applicationProperties.getManagerSystemFtps().getPort(),
                    applicationProperties.getManagerSystemFtps().getUserName(),
                    applicationProperties.getManagerSystemFtps().getPassword());
        } catch (Exception e) {
            log.error("上传至ftps错误: " + e);
        }
    }

    public boolean fileExits(String filepath){
        return FtpsUtil.isFTPFileExist(filepath,applicationProperties.getManagerSystemFtps().getIp()
                , applicationProperties.getManagerSystemFtps().getPort(),
                applicationProperties.getManagerSystemFtps().getUserName(),
                applicationProperties.getManagerSystemFtps().getPassword());
    }

}
