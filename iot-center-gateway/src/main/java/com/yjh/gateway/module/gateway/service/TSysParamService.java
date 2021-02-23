package com.yjh.gateway.module.gateway.service;


import com.yjh.gateway.module.gateway.dao.TSysParamDao;
import com.yjh.gateway.module.gateway.entity.TSysParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;


@Service
public class TSysParamService {

    @Resource
    private TSysParamDao tSysParamDao;


    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryCode() {
        return this.tSysParamDao.selectByPrimaryCode();
    }

}

