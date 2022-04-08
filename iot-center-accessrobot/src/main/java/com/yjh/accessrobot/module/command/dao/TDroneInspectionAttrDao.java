package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TDroneInspectionAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author hyh
 * @since 2022/2/17
 **/
@Repository
public interface TDroneInspectionAttrDao {

    int batchInsertTDroneInspectionAttr(List<TDroneInspectionAttr> list);

    List<TDroneInspectionAttr> selectByInspectionCode(@Param(value = "inspectionCode") String inspectionCode);

    int deleteByDroneId(@Param("droneId") Long droneId);
}
