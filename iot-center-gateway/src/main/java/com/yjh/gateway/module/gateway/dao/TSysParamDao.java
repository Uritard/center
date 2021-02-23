package com.yjh.gateway.module.gateway.dao;


import com.yjh.gateway.module.gateway.entity.TSysParam;
import org.springframework.stereotype.Repository;


@Repository
public interface TSysParamDao {


    TSysParam selectByPrimaryCode();

}
