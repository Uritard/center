package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
* @author yanhao
* @description 针对表【t_warn_info(告警信息表)】的数据库操作Mapper
* @createDate 2022-11-23
* @Entity com.yjh.accessrobot.module.command.entity.TWarnInfo
*/
@Repository
public interface TWarnInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TWarnInfo record);

    int insertSelective(TWarnInfo record);

    TWarnInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TWarnInfo record);

    int updateByPrimaryKey(TWarnInfo record);

    String selectAlarmLevel( @Param(value = "colName")String colName,
                             @Param(value = "dictNote")String dictNote);

}
