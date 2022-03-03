package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TRobotInspectionAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hyh
 * @since 2022/2/17
 **/
@Repository
public interface TRobotInspectionAttrDao {

    int batchInsertTRobotInspectionAttr(List<TRobotInspectionAttr> list);

    List<TRobotInspectionAttr> selectByInspectionCode(@Param(value = "inspectionCode") String inspectionCode);

    int deleteByRobotId(@Param("robotId") Long robotId);
}
