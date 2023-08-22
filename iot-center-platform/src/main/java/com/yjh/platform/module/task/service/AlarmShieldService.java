package com.yjh.platform.module.task.service;

import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.user.dao.AlarmShieldDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2023/08/15
 */

@Service
public class AlarmShieldService {

    @Autowired
    private AlarmShieldDao alarmShieldDao;
    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    public Boolean isShield(Long stdDeviceMeteId){
        TStdDeviceMete stdDeviceMete = tStdDevicemeteDao.selectByPrimaryId(stdDeviceMeteId);
        List<Long> shieldList= alarmShieldDao.selectAlarmShieldByStdDeviceMete(
                stdDeviceMete.getMeterType(),stdDeviceMete.getMeteType(),stdDeviceMeteId,new Date()
        );
        return !shieldList.isEmpty();
    }
}
