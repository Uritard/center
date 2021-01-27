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

    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdMeteModel(List<TStdMeteModel> tStdMeteModelList){
        return this.templateToImportDao.batchUpdateTStdMeteModel(tStdMeteModelList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdMeteModelDetail(List<TStdMeteModelDetail> tStdMeteModelDetailList){
        return this.templateToImportDao.batchUpdateTStdMeteModelDetail(tStdMeteModelDetailList);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long selectLastModelId(){
        return this.templateToImportDao.selectLastModelId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDevice(List<TStdDevice> tStdDeviceList){
        return this.templateToImportDao.batchUpdateTStdDevice(tStdDeviceList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDeviceAttr(List<TStdDeviceAttr> tStdDeviceAttrList){
        return this.templateToImportDao.batchUpdateTStdDeviceAttr(tStdDeviceAttrList);
    }
    @Transactional(rollbackFor = Exception.class)
    public Long selectLastDeviceId(){
        return this.templateToImportDao.selectLastDeviceId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdDeviceMete(List<TStdDeviceMete> tStdDeviceMeteList){
        return this.templateToImportDao.batchUpdateTStdDeviceMete(tStdDeviceMeteList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysOrg(List<SysOrg> sysOrgList){
        return this.templateToImportDao.batchUpdateSysOrg(sysOrgList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysUser(List<SysUser> sysUserList){
        return this.templateToImportDao.batchUpdateSysUser(sysUserList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysUserBackup(List<SysUserBackUp> sysUserBackupList){
        return this.templateToImportDao.batchUpdateSysUserBackup(sysUserBackupList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTRobotRegion(List<TRobotRegion> tRobotRegionList){
        return this.templateToImportDao.batchUpdateTRobotRegion(tRobotRegionList);
    }
    @Transactional(rollbackFor = Exception.class)
    public Long selectLastRegionId(){
        return this.templateToImportDao.selectLastRegionId();
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTStdRegion(List<TStdRegion> tStdRegionList){
        return this.templateToImportDao.batchUpdateTStdRegion(tStdRegionList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateSysRoleRegion(List<SysRoleRegion> sysRoleRegionList){
        return this.templateToImportDao.batchUpdateSysRoleRegion(sysRoleRegionList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTCfgTelemeter(List<TCfgTelemeter> tCfgTelemeterList){
        return this.templateToImportDao.batchUpdateTCfgTelemeter(tCfgTelemeterList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTCfgTelesignal(List<TCfgTelesignal> tCfgTelesignalList){
        return this.templateToImportDao.batchUpdateTCfgTelesignal(tCfgTelesignalList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTAlgorithmInfo(List<TAlgorithmInfo> tAlgorithmInfoList){
        return this.templateToImportDao.batchUpdateTAlgorithmInfo(tAlgorithmInfoList);
    }
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTAlgorithmConf(List<TAlgorithmConf> tAlgorithmConfList){
        return this.templateToImportDao.batchUpdateTAlgorithmConf(tAlgorithmConfList);
    }
}
