package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/13 - 13:40
 */
@Repository
public interface TCameraOperateDao {

    //摄像机预置位查询
    List<TCameraPreset> selectAllByTCPid(@Param(value = "presetId") Long presetId);
    //摄像机预置位删除
    int deleteByTCPid(@Param(value = "presetId") Long presetId);
    //摄像机预置位插入
    int insertTCP(TCameraPreset tCameraPreset);
    //摄像机预置位更新
    int updateTCP(TCameraPreset tCameraPreset);
}
