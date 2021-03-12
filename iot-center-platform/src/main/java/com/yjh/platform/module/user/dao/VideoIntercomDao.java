package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.user.entity.SysMenu;
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.entity.VideoIntercom;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoIntercomDao {

    int insert(VideoIntercom videoIntercom);

    int deleteByPrimaryId(@Param(value = "videoIntercomId") Long videoIntercomId);

    int deleteVideoIntercom(@Param(value = "list")List<String> list);

    int update(VideoIntercom videoIntercom);

     VideoIntercom  selectByPrimaryId(@Param(value = "videoIntercomId") Long videoIntercomId);

    List<VideoIntercom> selectByRegionId(@Param(value = "regionId") Long regionId);

    List<VideoIntercom> selectByPage( @Param(value = "cameraName") String cameraName,
                                         @Param(value = "regionIdList") List<Long> regionIdList);
}
