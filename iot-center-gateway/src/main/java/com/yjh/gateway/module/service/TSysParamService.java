package com.yjh.gateway.module.service;


import com.yjh.gateway.module.dao.TSysParamDao;
import com.yjh.gateway.module.entity.TSysParam;
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


    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryUkey() {
        return this.tSysParamDao.selectByPrimaryUkey();
    }
}

