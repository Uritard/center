package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.TWiringDiagram;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author 丫C
 * @since 2023-06-19
 */
@Repository
public interface TWiringDiagramDao {

    /**
     * 添加主接线图
     *
     * @param tWiringDiagram 主接线图信息
     * @return int
     */
    int insert(TWiringDiagram tWiringDiagram);

    /**
     * 更新主接线图信息
     *
     * @param tWiringDiagram 主接线图信息
     * @return int
     */
    int update(TWiringDiagram tWiringDiagram);

    /**
     * 根据主键查询主接线图信息
     *
     * @param wiringDiagramId 主键id
     * @return TWiringDiagram
     */
    TWiringDiagram selectByPrimaryId(@Param(value = "wiringDiagramId") Integer wiringDiagramId);

    /**
     * 条件查询主接线图信息
     *
     * @param regionId 区域id
     * @return TWiringDiagram
     */
    TWiringDiagram selectByCondition(@Param(value = "regionId")Long regionId);
}
