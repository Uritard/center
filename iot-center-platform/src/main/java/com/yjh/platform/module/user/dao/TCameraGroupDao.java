package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.user.entity.TCameraGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-11-17
 */
@Repository
public interface TCameraGroupDao {

    int add(TCameraGroup tCameraGroup);
    int deleteByPrimaryId(@Param(value = "groupId") Long groupId);
    int update(TCameraGroup tCameraGroup);
    TCameraGroup selectByPrimaryId(@Param(value = "groupId") Long groupId);
    List<TCameraGroup> select(@Param(value = "groupId") Long groupId,
                                @Param(value = "groupName") String groupName,
                                @Param(value = "cameraIds") String cameraIds,
                                @Param(value = "remarks") String remarks);
    List<TCameraGroup> selectByPage(TCameraGroup tCameraGroup);

    int batchAdd(List<TCameraGroup> list);
    int batchDelete(List<String> list);
    TCameraGroup selectIsExit(@Param(value = "groupName") String groupName);
    List<TCameraGroup> selectAll();
    List<Map<String,Object>>selectGroupName();
    String selectCameraIdInfo(@Param(value = "groupId") Long groupId);
}
