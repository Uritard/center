package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.VideoIntercomDao;
import com.yjh.platform.module.user.entity.VideoIntercom;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class VideoIntercomService {

    @Autowired
    private VideoIntercomDao  videoIntercomDao;


    public int insert(VideoIntercom videoIntercom)
    {
        return videoIntercomDao.insert(videoIntercom);
    }

    public  int deleteByPrimaryId(@Param(value = "videoIntercomId") Long videoIntercomId)
    {
        return videoIntercomDao.deleteByPrimaryId(videoIntercomId) ;
    }

    public int update(VideoIntercom videoIntercom)
    {
        return videoIntercomDao.update(videoIntercom);
    }


    public   int deleteVideoIntercom(String videoIntercomIdS)
    {

        List<String> list = Arrays.asList(videoIntercomIdS.split(","));
        return videoIntercomDao.deleteVideoIntercom(list);
    }

    public VideoIntercom  selectByPrimaryId(@Param(value = "videoIntercomId") Long videoIntercomId)
    {
        return videoIntercomDao.selectByPrimaryId(videoIntercomId);
    }

    public List<VideoIntercom> selectByRegionId(@Param(value = "regionId") Long regionId)
    {
        return videoIntercomDao.selectByRegionId(regionId);
    }



   public List<VideoIntercom> selectByPage( @Param(value = "cameraName") String cameraName,
                                      @Param(value = "regionIdList") List<Long> regionIdList)
   {
       return videoIntercomDao.selectByPage(cameraName,regionIdList);
   }
}
