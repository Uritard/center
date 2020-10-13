package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.TCruisePointAttr;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-10-13
 */
@Repository
public interface TCruisePointAttrDao {

    int add(TCruisePointAttr tCruisePointAttr);
    int deleteByPrimaryId(@Param(value = "instanceId") Long instanceId);
    int update(TCruisePointAttr tCruisePointAttr);
    TCruisePointAttr selectByPrimaryId(@Param(value = "instanceId") Long instanceId);
    List<TCruisePointAttr> select(@Param(value = "instanceId") Long instanceId,
                                @Param(value = "instanceName") String instanceName,
                                @Param(value = "attrName") String attrName,
                                @Param(value = "attrValue") String attrValue,
                                @Param(value = "remark1") String remark1);
    List<TCruisePointAttr> selectByPage(TCruisePointAttr tCruisePointAttr);

    int batchAdd(List<TCruisePointAttr> list);
    int batchDelete(List<String> list);
    int deleteByInstanceId(@Param(value = "list")List<Long> list);
}
