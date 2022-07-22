package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdMeterTypeModelDao;
import com.yjh.platform.module.device.entity.TStdMeterTypeModel;
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
public class TStdMeterTypeModelService {

    @Resource
    private TStdMeterTypeModelDao tStdMeterTypeModelDao;

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeterTypeModel tStdMeterTypeModel) {
        return this.tStdMeterTypeModelDao.add(tStdMeterTypeModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long meterTypeId) {
        return this.tStdMeterTypeModelDao.deleteByPrimaryId(meterTypeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeterTypeModel tStdMeterTypeModel) {
        return this.tStdMeterTypeModelDao.update(tStdMeterTypeModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdMeterTypeModel selectByPrimaryId(Long id) {
        return this.tStdMeterTypeModelDao.selectByPrimaryId(id);
    }
}
