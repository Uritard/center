package com.yjh.accesstcp.module.device.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yjh.accesstcp.commons.utils.file.FileUtil;
import com.yjh.accesstcp.module.device.dao.UnionTaskDao;
import com.yjh.accesstcp.module.device.entity.TCfgUnionRule;
import com.yjh.accesstcp.module.device.entity.TCruisePlan;
import com.yjh.accesstcp.module.device.entity.UPatrolPlanAttr;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import com.yjh.accesstcp.module.device.utils.ValueUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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

@Slf4j
@Service
public class AnalysisUnionTaskFileService {

    @Autowired
    private FtpsUtil ftpsUtil;

    @Autowired
    private UnionTaskDao unionTaskDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void handleUnionTaskFile(String filePath) {
        log.info("handleUnionTaskFile begin,filePath:{}", filePath);
        try {
            String fileName = StringUtils.substringAfter(filePath, "/linkage/");
            log.info("联动配置文件 fileName {}", fileName);
            String localFilePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath","content") + "/linkage/" ;
            FileUtil.createDirectory(localFilePath);
            ftpsUtil.downloadFile(localFilePath + fileName, filePath);
            List<Map<String, Object>> itemList = readUnionTaskXml(localFilePath + fileName);
            if (CollectionUtils.isNotEmpty(itemList)){
                deleteData(itemList);
                saveData(itemList);
            }else {
                deleteAll();
                log.info("handleUnionTaskFile is empty !!!");
            }
            log.info("handleUnionTaskFile success,filePath:{}", filePath);
        } catch (Exception e) {
            log.error("handleUnionTaskFile failed,filePath:{},error message:", filePath, e);
        }
    }

    /**
     * 读取联动任务xml文件
     *
     * @param filePath 文件路径
     * @throws DocumentException DocumentException
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readUnionTaskXml(String filePath) throws DocumentException {
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

    /**
     * 联动信号数据入库
     *
     * @param itemList xml转化后的json
     */
    private void saveData(List<Map<String, Object>> itemList) {
        TCruisePlan tCruisePlan = new TCruisePlan();
        tCruisePlan.setPlanName("联动任务" + System.currentTimeMillis());

        Set<Long> instanceIdList = Sets.newHashSet();
        List<TCfgUnionRule> tCfgUnionRuleList = Lists.newArrayList();
        itemList.forEach(item -> {
            String instanceIds = String.valueOf(item.get("device_id"));
            instanceIdList.addAll(ValueUtil.stringToList(instanceIds, ",", Long::parseLong));
            TCfgUnionRule tCfgUnionRule = new TCfgUnionRule();
            tCfgUnionRule.setRuleName(String.valueOf(item.get("source_name")));
            String inputParam = String.valueOf(item.get("source_code"));
            tCfgUnionRule.setInputParam(inputParam);
            tCfgUnionRuleList.add(tCfgUnionRule);
        });
        unionTaskDao.insertPlan(tCruisePlan);
        Long planId = tCruisePlan.getPlanId();
        tCfgUnionRuleList.forEach(tCfgUnionRule -> tCfgUnionRule.setPlanId(planId));
        List<UPatrolPlanAttr> uPatrolPlanAttrList = unionTaskDao.selectPatrolPlan(planId, instanceIdList);
        unionTaskDao.batchInsertPlanAttr(uPatrolPlanAttrList);
        unionTaskDao.batchInsertRule(tCfgUnionRuleList);
    }

    /**
     * 清空数据
     */
    private void deleteData(List<Map<String, Object>> itemList) {
        Set<Long> planIdList = Sets.newHashSet(unionTaskDao.selectPlanId(itemList));
        if (CollectionUtils.isEmpty(planIdList)) {
            return;
        }
        unionTaskDao.deletePlan(planIdList);
        unionTaskDao.deletePlanAttr(planIdList);
        unionTaskDao.deleteUnionRule(planIdList);
    }
    private void deleteAll() {
        unionTaskDao.deleteAll();
    }
}
