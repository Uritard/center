package com.yjh.gateway.module.dao;


import com.yjh.gateway.module.entity.TSysParam;
import org.springframework.stereotype.Repository;


@Repository
public interface TSysParamDao {
    
    TSysParam selectByPrimaryCode();

    TSysParam selectByPrimaryUkey();
}
