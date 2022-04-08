package com.yjh.accessrobot.module.command.service;


import com.yjh.accessrobot.module.command.dao.TDroneInspectionAttrDao;
import com.yjh.accessrobot.module.command.dao.TDroneInspectionDao;
import com.yjh.accessrobot.module.command.entity.TDroneInspectionAttr;
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
public class TDroneInspectionAttrService {
    @Autowired
    private TDroneInspectionAttrDao tDroneInspectionAttrDao;

    @Autowired
    private TDroneInspectionDao tDroneInspectionDao;


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectByInspectionCode(String inspectionCode) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> inspectionMap = tDroneInspectionDao.selectInspection(inspectionCode);
        if (Objects.isNull(inspectionMap)) return map;
        List<TDroneInspectionAttr> droneInspectionAttrList = tDroneInspectionAttrDao.selectByInspectionCode(inspectionCode);
        map.put("propertyPicPath", inspectionMap.get("property_pic_path").toString());
        map.put("list", droneInspectionAttrList);
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDroneInspectionAttr> list) {
        return this.tDroneInspectionAttrDao.batchInsertTDroneInspectionAttr(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByDroneId(Long droneId) {
        this.tDroneInspectionAttrDao.deleteByDroneId(droneId);
    }
}
