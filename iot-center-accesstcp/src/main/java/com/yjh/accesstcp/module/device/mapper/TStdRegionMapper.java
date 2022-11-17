package com.yjh.accesstcp.module.device.mapper;

import com.yjh.accesstcp.module.device.entity.TStdRegion;

/**
* @author yanhao
* @description 针对表【t_std_region(标准区域表)】的数据库操作Mapper
* @createDate 2022-11-17 14:33:33
* @Entity com.yjh.accesstcp.module.device.entity.TStdRegion
*/
public interface TStdRegionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TStdRegion record);

    int insertSelective(TStdRegion record);

    TStdRegion selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TStdRegion record);

    int updateByPrimaryKey(TStdRegion record);

}
