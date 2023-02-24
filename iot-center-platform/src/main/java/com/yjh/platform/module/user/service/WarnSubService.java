package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.WarnSubDao;
import com.yjh.platform.module.user.entity.WarnSub;
import com.yjh.platform.module.user.entity.enums.SubWarnTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class WarnSubService {

    @Autowired
    private WarnSubDao warnSubDao;

    public boolean select(Long userId, String type, String level, Long warnId) {
        String alarmLevelString = warnSubDao.selectAlarmNote(warnId);
        if (StringUtils.isNotBlank(alarmLevelString)) {
            List<String> l = Arrays.asList(alarmLevelString.split(","));
            if (l.contains(level) && SubWarnTypeEnum.DEVICE.getTypeCode().equals(type)) {
                return true;
            }
        }
        WarnSub warnSub = warnSubDao.selectByUserId(userId);
        if (Objects.nonNull(warnSub) && StringUtils.isNotBlank(warnSub.getSubWarnType())) {
            List<String> subWarnTypeList = Arrays.asList(warnSub.getSubWarnType().split(","));
            if (SubWarnTypeEnum.SYSTEM.getTypeCode().equals(type) && subWarnTypeList.contains(type)) {
                return true;
            } else if (SubWarnTypeEnum.DEVICE.getTypeCode().equals(type) && StringUtils.isNotBlank(warnSub.getSubWarnLevel())) {
                List<String> subWarnLevelList = Arrays.asList(warnSub.getSubWarnLevel().split(","));
                if (subWarnLevelList.contains(level)) {
                    return true;
                }
            }
        }
        return false;
    }

    public WarnSub selectByUserId(Long userId){
        return warnSubDao.selectByUserId(userId);
    }

    public int deleteByUserId(Long userId) {
        return warnSubDao.deleteByUserId(userId);
    }

    public int add(WarnSub warnSub) {
        this.deleteByUserId(warnSub.getUserId());
        return warnSubDao.add(warnSub);
    }
}
