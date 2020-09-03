package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/26 - 10:51
 */
@Repository
public interface TemplateToImportDao {
    //测点模板导入
    int batchUpdateTStdMeteModel(@Param(value = "tStdMeteModelList") List<TStdMeteModel> tStdMeteModelList);
    //测点模板细详细导入
    int batchUpdateTStdMeteModelDetail(@Param(value = "tStdMeteModelDetailList")List<TStdMeteModelDetail> tStdMeteModelDetailList);
    //获取模板表的最后一条modelId
    Long selectLastModelId();
    //标准化设备导入
    int batchUpdateTStdDevice(@Param(value = "tStdDeviceList")List<TStdDevice> tStdDeviceList);
    //标准化设备参数导入
    int batchUpdateTStdDeviceAttr(@Param(value = "tStdDeviceAttrList")List<TStdDeviceAttr> tStdDeviceAttrList);
    //获取标准化设备表的最后一条DeviceId
    Long selectLastDeviceId();
    //标准化设备测点导入
    int batchUpdateTStdDeviceMete(@Param(value = "tStdDeviceMeteList")List<TStdDeviceMete> tStdDeviceMeteList);
    //组织数据导入
    int batchUpdateSysOrg(@Param(value = "sysOrgList")List<SysOrg> sysOrgList);
    //系统用户数据导入
    int batchUpdateSysUser(@Param(value = "sysUserList")List<SysUser> sysUserList);
    //用户备份数据导入
    int batchUpdateSysUserBackup(@Param(value = "sysUserBackupList")List<SysUserBackUp> sysUserBackupList);
    //机器人区域数据导入
    int batchUpdateTRobotRegion(@Param(value = "tRobotRegionList")List<TRobotRegion> tRobotRegionList);
    //获取标准区域表的最后一条regionId
    Long selectLastRegionId();
    //标准区域数据导入
    int batchUpdateTStdRegion(@Param(value = "tStdRegionList")List<TStdRegion> tStdRegionList);
    //权限区域数据导入
    int batchUpdateSysRoleRegion(@Param(value = "sysRoleRegionList")List<SysRoleRegion> sysRoleRegionList);
    //遥测量数据导入
    int batchUpdateTCfgTelemeter(@Param(value = "tCfgTelemeterList")List<TCfgTelemeter> tCfgTelemeterList);
    //遥信量数据导入
    int batchUpdateTCfgTelesignal(@Param(value = "tCfgTelesignalList")List<TCfgTelesignal> tCfgTelesignalList);
    //算法数据导入
    int batchUpdateTAlgorithmInfo(@Param(value = "tAlgorithmInfoList")List<TAlgorithmInfo> tAlgorithmInfoList);
    //算法配置导入
    int batchUpdateTAlgorithmConf(@Param(value = "tAlgorithmConfList")List<TAlgorithmConf> tAlgorithmConfList);
}
