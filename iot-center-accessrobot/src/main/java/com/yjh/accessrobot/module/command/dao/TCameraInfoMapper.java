package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.TCameraInfo;

/**
* @author yanhao
* @description 针对表【t_camera_info(摄像头信息表)】的数据库操作Mapper
* @createDate 2022-11-09 16:08:05
* @Entity com.yjh.accessrobot.module.command.entity.TCameraInfo
*/
public interface TCameraInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TCameraInfo record);

    int insertSelective(TCameraInfo record);

    TCameraInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraInfo record);

    int updateByPrimaryKey(TCameraInfo record);

}
