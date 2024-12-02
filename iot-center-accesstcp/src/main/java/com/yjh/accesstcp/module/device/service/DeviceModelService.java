package com.yjh.accesstcp.module.device.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yjh.accesstcp.commons.utils.CommonUtils;
import com.yjh.accesstcp.commons.utils.file.FileUtil;
import com.yjh.accesstcp.module.device.dao.DeviceModelDao;
import com.yjh.accesstcp.module.device.entity.TCfgUnionRule;
import com.yjh.accesstcp.module.device.entity.TCruisePlan;
import com.yjh.accesstcp.module.device.entity.TStdMete;
import com.yjh.accesstcp.module.device.entity.UPatrolPlanAttr;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.module.device.utils.ValueUtil;
import com.yjh.platform.module.task.entity.TStdDevicemete;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DeviceModelService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DeviceModelDao deviceModelDao;
    @Transactional(rollbackFor = Exception.class)
    public void handleDeviceModelFile(String filePath,String command) {
        log.info("handleDeviceModelFile begin,filePath:{}", filePath);
        try {
            String fileName = StringUtils.substringAfterLast(filePath, "/");
            log.info("设备模型文件 fileName {}", fileName);
            String localFilePath = redisTemplate.opsForHash().get("t_sys_param:modelAbsolutePath","content") + "/deviceModel/" ;
            FileUtil.createDirectory(localFilePath);
            downloadFile(localFilePath + fileName, filePath);
            log.info("设备模型文件下载完成");
            List<Map<String, Object>> itemList = readDeviceModelXml(localFilePath + fileName);
            if (CollectionUtils.isNotEmpty(itemList)){
                switch (command) {
                    //<1>: =标准点位模型文件
                    case "1":
                        log.info("下载后标准点位模型文件 filePath {}", localFilePath + fileName);
                        saveMeteData(itemList);
                        break;
                    case "2":
                        log.info("下载后巡视装置模型文件 filePath {}", localFilePath + fileName);
                        break;
                    case "3":
                        log.info("下载后点位告警阈值配置模型文件 filePath {}", localFilePath + fileName);
//                        saveMeteAlarmData(itemList);
                        break;
                    default:
                        break;
                }

            }else {
                log.info("DeviceModelFile is empty !!!");
            }
            log.info("handleDeviceMOdelFile success,filePath:{}", filePath);
        } catch (Exception e) {
            log.error("handleDeviceMOdelFile failed,filePath:{},error message:", filePath, e);
        }
    }

    /**
     * ftps下载
     * @param sourcePath
     * @param targetPathName
     */
    private void downloadFile(String sourcePath, String targetPathName) {
        try {
            if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {
                return;
            }
            Map<String, String> upSystemFtps = redisTemplate.opsForHash().entries("systemConfigKey:upSystem");
            String upSystemFtpsIp = upSystemFtps.get("upSystemFtpsIp");
            String upSystemFtpsPort = upSystemFtps.get("upSystemFtpsPort");
            String upSystemFtpsUsername = upSystemFtps.get("upSystemFtpsUsername");
            String upSystemFtpsPassword = upSystemFtps.get("upSystemFtpsPassword");
            boolean resolveLocal = Boolean.parseBoolean(upSystemFtps.get("upSystemFtpsResolveLocal"));
            FtpsUtil.downloadFile(sourcePath, targetPathName, upSystemFtpsIp, Integer.parseInt(upSystemFtpsPort),
                    upSystemFtpsUsername, upSystemFtpsPassword, resolveLocal);
        } catch (Exception e) {
            log.error("将文件从上级系统ftp服务器下载错误：", e);
        }
    }

    public List<Map<String, Object>> readDeviceModelXml(String filePath) throws DocumentException{
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePath));
        Element rootElement = document.getRootElement();
        List<Element> items = rootElement.elements("Item");
        List<Map<String, Object>> mapList= new ArrayList<>();
        for (Element item : items) {
            Map<String, Object> map = new HashMap<>();
            for (Object o : item.attributes()) {
                Attribute attr = (Attribute) o;
                if (StringUtils.isNotBlank(attr.getValue())) {
                    map.put(attr.getName(), attr.getValue());
                }
            }
            mapList.add(map);
        }
        return mapList;
    }

    private void saveMeteData(List<Map<String, Object>> itemList) {
        List<TStdMete> tCfgUnionRuleList = new ArrayList<>();
        List<Map<String, String>> dictMapList = deviceModelDao.selectDictCodeByUpDict("device_type,mete_type");
        itemList.forEach(item -> {
            if (!CommonUtils.isEmptyOrNullstr(String.valueOf(item.get("standard_device_id")))) {
                TStdMete tStdMete = new TStdMete();
                tStdMete.setRemark(String.valueOf(item.get("standard_device_id")));
                tStdMete.setDeviceType(CollectionUtils.isNotEmpty(dictMapList) ?
                        Integer.parseInt(dictMapList.stream().filter(s -> "device_type".equals(s.get("col_name"))
                                        && item.get("device_type").equals(String.valueOf(s.get("up_dict")))).map(d -> d.get("dict_code"))
                                .collect(Collectors.toList()).get(0)) : 194);
                String recognitionTypeList = String.valueOf(item.get("recognition_type_list"));
                String meteType = "219";
                if (!CommonUtils.isEmptyOrNullstr(recognitionTypeList)){
                    String[] strings = recognitionTypeList.split(",");
                    meteType = CollectionUtils.isNotEmpty(dictMapList) ?
                            dictMapList.stream().filter(s -> "mete_type".equals(s.get("col_name"))
                                    && strings[0].equals(String.valueOf(s.get("up_dict")))).map(d -> d.get("dict_code"))
                                    .collect(Collectors.toList()).get(0) : "219";
                }
                tStdMete.setMeteType(meteType);
                tStdMete.setMeteName((String) item.get("standard_device_name"));
                tStdMete.setRedundantType((String) item.get("point_type"));
                tCfgUnionRuleList.add(tStdMete);
            }
        });
        if (tCfgUnionRuleList.size() > 0){
            deviceModelDao.cleanAll();
            deviceModelDao.batchAdd(tCfgUnionRuleList);
            log.info("标准点位模型文件导入数据成功，共{}条", tCfgUnionRuleList.size());
        }
    }

    private void saveMeteAlarmData(List<Map<String, Object>> itemList) {
        List<TStdDevicemete> tStdDevicemeteList = new ArrayList<>();
        itemList.forEach(item -> {
            if (item.get("device_id") != null) {
                TStdDevicemete tStdDevicemete = new TStdDevicemete();
                tStdDevicemete.setDeviceMeteId(NumberUtils.toLong((String) item.get("device_id")));
                tStdDevicemete.setMeteKind(String.valueOf(0));
                tStdDevicemete.setAlarmType((String) item.get("alarm_type"));
                tStdDevicemete.setRemark((String) item.get("alarm_desc"));
                tStdDevicemete.setAlarmLevel(NumberUtils.toInt((String) item.get("alarm_level")));
                tStdDevicemete.setRedundantType("1");
                tStdDevicemeteList.add(tStdDevicemete);
            }
        });
        if (tStdDevicemeteList.size() > 0){
            deviceModelDao.batchAddDeviceMetes(tStdDevicemeteList);
            log.info("点位告警阈值文件导入数据成功，共{}条", tStdDevicemeteList.size());
        }
    }

}
