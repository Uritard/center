package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdDeviceTypeModelDao;
import com.yjh.platform.module.device.entity.TStdDeviceTypeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author hyh
 * @since 2022/4/15
 **/
@Service
@Slf4j
public class TStdDeviceTypeModelService {

    @Resource
    private TStdDeviceTypeModelDao tStdDeviceTypeModelDao;

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdDeviceTypeModel tStdDeviceTypeModel) {
        return this.tStdDeviceTypeModelDao.add(tStdDeviceTypeModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceTypeId) {
        return this.tStdDeviceTypeModelDao.deleteByPrimaryId(deviceTypeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdDeviceTypeModel tStdDeviceTypeModel) {
        return this.tStdDeviceTypeModelDao.update(tStdDeviceTypeModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceTypeModel selectByPrimaryId(Long deviceTypeId) {
        return this.tStdDeviceTypeModelDao.selectByPrimaryId(deviceTypeId);
    }
}
