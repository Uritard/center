package com.yjh.platform.module.udp.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TCfgMeteDao;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/05/15
 */
@Slf4j
@Service
public class CfgMeteService {

    @Autowired
    private TCfgMeteDao tCfgMeteDao;


    public void dataCall(List<Long> meteIds, String filePath){
        try {
            List<TCfgDataCurrent> meteList = tCfgMeteDao.selectByMeteIds(meteIds);

            String path = filePath + "datacall.cime";
            File cimeFile = new File(path);
            if (cimeFile.delete()) {
                cimeFile.delete();
            }
            if (!cimeFile.exists()) {
                cimeFile.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(cimeFile, true)));
            bw.write("<!Entity=数据召唤请求\tver='V1.0'\ttime='" + DateTimeUtil.format(new Date()) + "'(文件最新时间)!>\r\n");
            bw.write("<DeviceInfo::召唤数据信息>\r\n");
            bw.write("@序号\t站序号\t监控索引号\t设备名称\t类型\r\n");
            for (int i = 0; i < meteList.size(); i++) {
                bw.write("#" + (i+1) +"\t" + meteList.get(i).getMeteId() + "\t" + meteList.get(i).getDeviceName() + "\t" + dealMeteKind(meteList.get(i).getMeteKind()) + "\r\n");
            }
            bw.write("</DeviceInfo::召唤数据信息>\r\n");
        }catch (Exception e){
            log.error("生成数据召唤文件失败：",e);
        }
    }

    private String dealMeteKind(Integer meteKind){
        switch (meteKind){
            case 3:
                return "遥控";
            case 1:
                return "遥信";
            case 2:
                return "遥测";

        }
        return "";
    }
}
