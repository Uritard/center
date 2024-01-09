package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TWiringConfig;
import com.yjh.platform.module.user.entity.TWiringConfigEX;
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
     * 根据机器人去看看图id查询地图上对应的设备点位信息和对应的机器人点位inspection_code
     * @param wiringDiagramId
     * @param inspectionType  ==2 只查有操作点的设备
     * @return
     */
    List<TWiringConfigEX> selectByWiringDiagram(@Param(value = "wiringDiagramId") Long wiringDiagramId,
                                                @Param(value = "inspectionType") Integer inspectionType);

    /**
     * 条件查询主接线图关系信息
     *
     * @param regionId 区域id
     * @return List<TWiringConfigVo>
     */
    List<TWiringConfigVo> selectByCondition(@Param(value = "regionId")Long regionId,
                                            @Param(value = "robotId")Long robotId,
                                            @Param(value = "type")Integer type);
}
