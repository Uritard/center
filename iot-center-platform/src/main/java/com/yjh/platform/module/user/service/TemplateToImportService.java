package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.device.entity.TStdDeviceAttr;
import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import com.yjh.platform.module.user.dao.TemplateToImportDao;
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

}
