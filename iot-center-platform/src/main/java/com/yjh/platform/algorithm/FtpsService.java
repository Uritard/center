package com.yjh.platform.algorithm;

import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
public class FtpsService {

    @Autowired
    private ApplicationProperties applicationProperties;

    private Logger log = LoggerFactory.getLogger(FtpsService.class);

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
            log.error("上传至ftps错误: {}, {}", applicationProperties.getManagerSystemFtps(), e);
        }
    }

    public void downLoadFile(String filePath,String remoteFileName) {
        try {
            FtpsUtil.downloadFile(filePath, remoteFileName, applicationProperties.getManagerSystemFtps().getIp()
                    , applicationProperties.getManagerSystemFtps().getPort(),
                    applicationProperties.getManagerSystemFtps().getUserName(),
                    applicationProperties.getManagerSystemFtps().getPassword());
        } catch (Exception e) {
            log.error("下载ftps错误: {}, {}", applicationProperties.getManagerSystemFtps(), e);
        }
    }

    public boolean fileExits(String filepath){
        return FtpsUtil.isFTPFileExist(filepath,applicationProperties.getManagerSystemFtps().getIp()
                , applicationProperties.getManagerSystemFtps().getPort(),
                applicationProperties.getManagerSystemFtps().getUserName(),
                applicationProperties.getManagerSystemFtps().getPassword());
    }


    /**
     * 将图片从ftps下载到本地
     *
     * @param ftpsPath ftpsPath
     * @param localPath localPath
     */
    public void downloadAnalysisFile(String ftpsPath, String localPath){
        log.info("将图片从ftps下载到本地, ftpsPath: {}, localPath: {}", ftpsPath, localPath);
        try {
            if(StringUtils.isEmpty(ftpsPath) || StringUtils.isEmpty(localPath)) {
                log.info("ftpsPath或localPath为空，返回");
                return;
            }

            log.info("开始执行：FtpsUtil.downloadFile");
            FtpsUtil.downloadFile(localPath, ftpsPath, applicationProperties.getIntelAnalysisFtps().getIp(), applicationProperties.getIntelAnalysisFtps().getPort(), applicationProperties.getIntelAnalysisFtps().getUserName(), applicationProperties.getIntelAnalysisFtps().getPassword());
            log.info("结束执行：FtpsUtil.downloadFile");
        } catch (Exception e) {
            log.error("将文件从 platform ftp 服务器下载到本地错误: {}  {}", applicationProperties.getIntelAnalysisFtps(), e);
        }
    }

    /**
     * 将本地文件上传到ftps
     *
     * @param ftpsPath ftpsPath
     * @param localPath localPath
     */
    public void uploadAnalysisFile(String ftpsPath, String localPath){
        log.info("将本地文件上传到ftps, ftpsPath: {}, localPath: {}", ftpsPath, localPath);
        try {
            if(StringUtils.isEmpty(ftpsPath) || StringUtils.isEmpty(localPath)) {
                log.info("ftpsPath或localPath为空，返回");
                return;
            }

            log.info("开始执行：FtpsUtil.putFile");
            FtpsUtil.putFile(localPath, ftpsPath, applicationProperties.getIntelAnalysisFtps().getIp(), applicationProperties.getIntelAnalysisFtps().getPort() , applicationProperties.getIntelAnalysisFtps().getUserName(), applicationProperties.getIntelAnalysisFtps().getPassword());
            log.info("结束执行：FtpsUtil.putFile");
        } catch (Exception e) {
            log.error("将文件上传至 platform ftp 服务器错误: {}  {}", applicationProperties.getIntelAnalysisFtps(), e);
        }
    }

}
