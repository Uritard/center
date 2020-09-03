package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.dao.TemplateToImportDao;
import com.yjh.platform.module.user.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/26 - 10:43
 */
@Service
public class TemplateToImportService {

    @Autowired
    private TemplateToImportDao templateToImportDao;

    @Logs(title = "测点模板导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdMeteModel(List<TStdMeteModel> tStdMeteModelList){
        return this.templateToImportDao.batchUpdateTStdMeteModel(tStdMeteModelList);
    }

    @Logs(title = "测点模板详细导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdMeteModelDetail(List<TStdMeteModelDetail> tStdMeteModelDetailList){
        return this.templateToImportDao.batchUpdateTStdMeteModelDetail(tStdMeteModelDetailList);
    }

    @Logs(title = "获取最后一条数据的modelId",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Long selectLastModelId(){
        return this.templateToImportDao.selectLastModelId();
    }

    @Logs(title = "标准化设备导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDevice(List<TStdDevice> tStdDeviceList){
        return this.templateToImportDao.batchUpdateTStdDevice(tStdDeviceList);
    }
    @Logs(title = "标准化设备参数导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDeviceAttr(List<TStdDeviceAttr> tStdDeviceAttrList){
        return this.templateToImportDao.batchUpdateTStdDeviceAttr(tStdDeviceAttrList);
    }
    @Logs(title = "获取标准化设备表的最后一条DeviceId",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Long selectLastDeviceId(){
        return this.templateToImportDao.selectLastDeviceId();
    }

    @Logs(title = "标准化设备测点导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDeviceMete(List<TStdDeviceMete> tStdDeviceMeteList){
        return this.templateToImportDao.batchUpdateTStdDeviceMete(tStdDeviceMeteList);
    }
    @Logs(title = "组织数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysOrg(List<SysOrg> sysOrgList){
        return this.templateToImportDao.batchUpdateSysOrg(sysOrgList);
    }
    @Logs(title = "系统用户数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysUser(List<SysUser> sysUserList){
        return this.templateToImportDao.batchUpdateSysUser(sysUserList);
    }
    @Logs(title = "用户备份数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysUserBackup(List<SysUserBackUp> sysUserBackupList){
        return this.templateToImportDao.batchUpdateSysUserBackup(sysUserBackupList);
    }
    @Logs(title = "机器人区域数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTRobotRegion(List<TRobotRegion> tRobotRegionList){
        return this.templateToImportDao.batchUpdateTRobotRegion(tRobotRegionList);
    }
    @Logs(title = "获取标准区域表的最后一条regionId",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Long selectLastRegionId(){
        return this.templateToImportDao.selectLastRegionId();
    }
    @Logs(title = "标准区域数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdRegion(List<TStdRegion> tStdRegionList){
        return this.templateToImportDao.batchUpdateTStdRegion(tStdRegionList);
    }
    @Logs(title = "权限区域数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysRoleRegion(List<SysRoleRegion> sysRoleRegionList){
        return this.templateToImportDao.batchUpdateSysRoleRegion(sysRoleRegionList);
    }
    @Logs(title = "遥测量数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTCfgTelemeter(List<TCfgTelemeter> tCfgTelemeterList){
        return this.templateToImportDao.batchUpdateTCfgTelemeter(tCfgTelemeterList);
    }
    @Logs(title = "遥信量数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTCfgTelesignal(List<TCfgTelesignal> tCfgTelesignalList){
        return this.templateToImportDao.batchUpdateTCfgTelesignal(tCfgTelesignalList);
    }
    @Logs(title = "算法数据导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTAlgorithmInfo(List<TAlgorithmInfo> tAlgorithmInfoList){
        return this.templateToImportDao.batchUpdateTAlgorithmInfo(tAlgorithmInfoList);
    }
    @Logs(title = "算法配置导入",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTAlgorithmConf(List<TAlgorithmConf> tAlgorithmConfList){
        return this.templateToImportDao.batchUpdateTAlgorithmConf(tAlgorithmConfList);
    }
}
