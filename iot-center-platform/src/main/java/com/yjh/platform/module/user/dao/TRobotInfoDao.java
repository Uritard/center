package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.TRobotInspectionTree;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-05
 */
@Repository
public interface TRobotInfoDao {

    int insert(TRobotInfo tRobotInfo);
    //多表删除 t_robot_info
    int deleteByPrimaryId(Long robotId);
    //多表删除 t_cruise_plan
    int deletePlan(Long robotId);
    //多表删除 t_robot_inspection
    int deleteInspection(Long robotId);
    //多表删除 t_cruise_point_instance
    int deleteInstance(Long robotId);

    int update(TRobotInfo tRobotInfo);
    TRobotInfo selectByPrimaryId(@Param(value = "robotId") Long robotId);
    List<TRobotInfo> select(@Param(value = "robotId") Long robotId,
                            @Param(value = "robotCode") String robotCode,
                                @Param(value = "robotName") String robotName,
                                @Param(value = "robotStatus") String robotStatus,
                                @Param(value = "robotType") Integer robotType,
                                @Param(value = "robotIp") String robotIp,
                                @Param(value = "robotPort") Integer robotPort,
                                @Param(value = "upRegionName") String upRegionName,
                                @Param(value = "lightIp") String lightIp,
                                @Param(value = "lightPort") String lightPort,
                                @Param(value = "lightUsername") String lightUsername,
                                @Param(value = "lightPassword") String lightPassword,
                                @Param(value = "lnferadIp") String lnferadIp,
                                @Param(value = "inferadPort") Integer inferadPort,
                                @Param(value = "inferadUsername") String inferadUsername,
                                @Param(value = "inferadPassword") String inferadPassword,
                                @Param(value = "photePath") String photePath,
                                @Param(value = "createBy") String createBy,
                                @Param(value = "createDate") Date createDate,
                                @Param(value = "updateBy") String updateBy,
                                @Param(value = "updateDate") Date updateDate,
                                @Param(value = "robotFactory") String robotFactory,
                                @Param(value = "isUse") String isUse,
                                @Param(value = "commissionDate") Date commissionDate,
                                @Param(value = "upRegionId") Long upRegionId,
                                @Param(value = "robotPosition") String robotPosition,
                                @Param(value = "remarks") String remarks);
    List<TRobotInfo> selectByPage(TRobotInfo tRobotInfo);

    int batchInsert(@Param("list") List<TRobotInfo> list);
    List<TRobotInspectionTree> selectInspectionTree();
    List<TRobotInspectionTree> batchSelectInspection();

}
