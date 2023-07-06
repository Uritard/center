package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TWiringConfig;
import com.yjh.platform.module.user.entity.TWiringConfigVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 丫C
 * @since 2023-06-19
 */
@Repository
public interface TWiringConfigDao {

    /**
     * 增加设备与主接线图的关联
     *
     * @param tWiringConfigList 关联关系
     * @return int
     */
    int insert(@Param("tWiringConfigList")List<TWiringConfigVo> tWiringConfigList);

    /**
     * 根据主键删除关联关系
     *
     * @param wiringConfigId 主键id
     * @return int
     */
    int deleteByPrimaryId(@Param("wiringConfigId")Long wiringConfigId);

    /**
     * 根据关联关系id删除关联关系
     *
     * @param wiringDiagramId 关联关系id
     * @return int
     */
    int deleteByWiringDiagramId(@Param("wiringDiagramId")Integer wiringDiagramId);

    /**
     * 更新设备与主接线图的关联
     *
     * @param tWiringConfigList 关联关系
     * @return int
     */
    int update(@Param("tWiringConfigList")List<TWiringConfig> tWiringConfigList);

    /**
     * 根据主键查询主接线图关联信息
     *
     * @param wiringConfigId 主键id
     * @return TWiringConfig
     */
    TWiringConfig selectByPrimaryId(@Param(value = "wiringConfigId") Long wiringConfigId);

    /**
     * 条件查询主接线图关系信息
     *
     * @param regionId 区域id
     * @return List<TWiringConfigVo>
     */
    List<TWiringConfigVo> selectByCondition(@Param(value = "regionId")Long regionId);
}
