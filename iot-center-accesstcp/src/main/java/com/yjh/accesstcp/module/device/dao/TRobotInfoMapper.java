package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TRobotInfo;

/**
* @author yanhao
* @description 针对表【t_robot_info(机器人表)】的数据库操作Mapper
* @createDate 2022-11-09 16:41:59
* @Entity com.yjh.accesstcp.module.device.entity.TRobotInfo
*/
public interface TRobotInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TRobotInfo record);

    int insertSelective(TRobotInfo record);

    TRobotInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TRobotInfo record);

    int updateByPrimaryKey(TRobotInfo record);

}
