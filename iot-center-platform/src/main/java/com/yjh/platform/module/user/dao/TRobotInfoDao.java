package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.task.entity.RobotInfoForHomePage;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.entity.TRobotInspectionTree;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-08-05
 */
@Repository
public interface TRobotInfoDao {

    int insert(TRobotInfo tRobotInfo);
    //多表删除 t_robot_info t_robot_region
    int deleteByPrimaryId(Long robotId);
    //多表删除 t_cruise_plan t_cruise_plan_attr t_cruise_point_instance 删除操作票信息
    int deleteCruisePlan(Long robotId);
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
                                @Param(value = "identityManager") String lightUsername,
                                @Param(value = "identityCode") String lightPassword,
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
                                @Param(value = "robotSource") String robotSource,
                                @Param(value = "address") String address,
                                @Param(value = "buildingUser") String buildingUser,
                                @Param(value = "appearanceNumber") String appearanceNumber,
                                @Param(value = "defectRecord") String defectRecord,
                                @Param(value = "repairRecord") String repairRecord,
                                @Param(value = "exitPutIntoRecord") String exitPutIntoRecord,
                                @Param(value = "remarks") String remarks);
    List<TRobotInfo> selectByPage(@Param(value = "robotName") String robotName,
                                @Param(value = "buildingUser") String buildingUser,
                                  @Param(value = "robotFactory") Integer robotFactory,
                                  @Param(value = "robotType") Integer robotType,
                                  @Param(value = "robotSource") String robotSource,
                                  @Param(value = "isUse") Integer isUse,
                                  @Param(value = "address") String address,
                                  @Param(value = "regionIdList") List<Long> regionIdList);

    int batchInsert(@Param("list") List<TRobotInfo> list);
    List<TRobotInspectionTree> selectInspectionTree(@Param(value = "upRegionId")  Long upRegionId);
    List<TRobotInspectionTree> batchSelectInspection(@Param(value = "inspectionType") Integer inspectionType,
                                                     @Param(value = "upRegionId") Long upRegionId);

    //通过巡视点ID查询绑定的机器人
    Long selectRobotScreen(@Param(value = "instanceId")Long instanceId);

    List<RobotInfoForHomePage>selectRobotInfo(@Param(value = "robotPosition")String robotPosition);

    String selectDictCode(@Param(value = "colName")String colName,
                          @Param(value = "dictNote")String dictNote);
    Long selectRobotIdByCode(@Param(value = "robotCode") String robotCode);
    String selectUserName(@Param(value = "userID")Long  userID);
    List<TRobotInfo> selectAllRobotCode();
    List<String> selectAllRobotCode2();
    String selectRobotCodeById(@Param(value = "robotId")Long robotId);
    List<Long>selectHaveIns(@Param(value = "robotId") Long robotId);

    //查询当前机器人执行当前巡视点时使用的有效工作摄像头类型
    String selectRobotRunningCamera(@Param(value = "robotId")Long robotId,@Param(value = "instanceId")Long instanceId);
}
