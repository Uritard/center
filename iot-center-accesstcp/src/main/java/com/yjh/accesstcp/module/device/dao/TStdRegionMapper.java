package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TStdRegion;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author yanhao
* @description 针对表【t_std_region(标准区域表)】的数据库操作Mapper
* @createDate 2022-11-17 14:34:01
* @Entity com.yjh.accesstcp.module.device.entity.TStdRegion
*/
@Repository
public interface TStdRegionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdRegion record);

    int insertSelective(TStdRegion record);

    TStdRegion selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdRegion record);

    int updateByPrimaryKey(TStdRegion record);

    List<TStdRegion> selectAll();

}
