package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.WarnSubDao;
import com.yjh.platform.module.user.entity.WarnSub;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WarnSubService {

    @Autowired
    private WarnSubDao warnSubDao;

    public boolean select(Long userId,String type,String level) {
        WarnSub warnSub = warnSubDao.selectByUserId(userId);
        if (warnSub == null){
            return true;
        }
        if (StringUtils.isEmpty(warnSub.getSubWarnType())){
            return true;
        }
        if (warnSub.getSubWarnType().contains(type)){
            if (warnSub.getSubWarnLevel().contains(level)){
                return true;
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
