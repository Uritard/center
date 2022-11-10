package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.CameraModel;
import com.yjh.accesstcp.module.device.entity.TCameraInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author yanhao
* @description 针对表【t_camera_info(摄像头信息表)】的数据库操作Mapper
* @createDate 2022-11-09
* @Entity com.yjh.accesstcp.module.device.entity.TCameraInfo
*/
@Repository
public interface TCameraInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TCameraInfo record);

    int insertSelective(TCameraInfo record);

    TCameraInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraInfo record);

    int updateByPrimaryKey(TCameraInfo record);

    List<CameraModel> selectAll();
}
