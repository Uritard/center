package com.yjh.accessmeter.module.dao;

import com.yjh.accessmeter.module.device.entity.TMeter;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author yanhao
* @description 针对表【t_meter】的数据库操作Mapper
* @createDate 2022-10-21
* @Entity com.yjh.platform.module.device.entity.TMeter
*/
@Repository
public interface TMeterDao {

    List<TMeter> selectAll();

    int deleteByPrimaryKey(Long id);

    int insert(TMeter record);

    TMeter selectByPrimaryKey(Long id);

    int updateData(TMeter record);

    int updateAddress(TMeter record);


}
