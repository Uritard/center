package com.yjh.platform.module.device.service;


import com.yjh.platform.module.device.dao.TRobotInspectionAttrDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TRobotInspectionAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hyh
 * @since 2022/2/17
 **/
@Service
public class TRobotInspectionAttrService {
    @Autowired
    private TRobotInspectionAttrDao tRobotInspectionAttrDao;

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectByInspectionCode(String inspectionCode) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> inspectionMap = tRobotInspectionDao.selectInspection(inspectionCode);
        if (Objects.isNull(inspectionMap)) return map;
        List<TRobotInspectionAttr> robotInspectionAttrList = tRobotInspectionAttrDao.selectByInspectionCode(inspectionCode);
        map.put("propertyPicPath", inspectionMap.get("property_pic_path").toString());
        map.put("list", robotInspectionAttrList);
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInspectionAttr> list) {
        return this.tRobotInspectionAttrDao.batchInsertTRobotInspectionAttr(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByRobotId(Long robotId) {
        this.tRobotInspectionAttrDao.deleteByRobotId(robotId);
    }
}
