package com.yjh.accesstcp.module.device.dao;


import com.yjh.accesstcp.module.device.entity.TCameraPreset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_camera_preset(摄像机预位置表)】的数据库操作Mapper
* @createDate 2022-11-11 11:05:16
* @Entity generator.domain.TCameraPreset
*/
@Repository
public interface TCameraPresetMapper {

    TCameraPreset selectByPrimaryKey(Long id);

    int updatePtzByPrimaryKey(TCameraPreset record);
}
