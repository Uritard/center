package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.smUtil.ModelDecodeUtil;
import com.yjh.platform.module.user.dao.VideoIntercomDao;
import com.yjh.platform.module.user.entity.VideoIntercom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VideoIntercomService {

    @Autowired
    private VideoIntercomDao  videoIntercomDao;
    @Value("${video.videoUrl.path}")
    private  String videoUrl;


    public int insert(VideoIntercom videoIntercom)
    {
        ModelDecodeUtil.decodeField(videoIntercom, "ownerCode");
        return videoIntercomDao.insert(videoIntercom);
    }

    public  int deleteByPrimaryId(@Param(value = "videoIntercomId") Long videoIntercomId)
    {
        return videoIntercomDao.deleteByPrimaryId(videoIntercomId) ;
    }

    public int update(VideoIntercom videoIntercom)
    {
        ModelDecodeUtil.decodeField(videoIntercom, "ownerCode");

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
       List<VideoIntercom> lists=new ArrayList<>();
       List<VideoIntercom> list =videoIntercomDao.selectByPage(cameraName,regionIdList);
      try {

          for (VideoIntercom t:list)
          {

              VideoIntercom videoIntercom=new VideoIntercom();
              int state = videoState(videoIntercom.getVideoIntercomId());
              videoIntercom.setState(state);
              videoIntercom.setVideoIntercomId(t.getVideoIntercomId());
              videoIntercom.setOwner(t.getOwner());
              videoIntercom.setAddress(t.getAddress());
              videoIntercom.setCameraIp(t.getCameraIp());
              videoIntercom.setCameraModel(t.getCameraModel());
              videoIntercom.setCameraName(t.getCameraName());
              videoIntercom.setCameraType(t.getCameraType());
              videoIntercom.setChannelNum(t.getChannelNum());
              videoIntercom.setOwnerCode(t.getOwnerCode());
              videoIntercom.setPort(t.getPort());
              videoIntercom.setProtocolType(t.getProtocolType());
              videoIntercom.setRegionName(t.getRegionName());
              videoIntercom.setRemark(t.getRemark());
              videoIntercom.setRtspPort(t.getRtspPort());
              videoIntercom.setUpRegionId(t.getUpRegionId());
              videoIntercom.setVendor(t.getVendor());
              videoIntercom.setVendorId(t.getVendorId());
              lists.add(videoIntercom);

          }
      } catch (Exception e) {
          log.error(e.getMessage(), e);
      }
      return lists;

      //return videoIntercomDao.selectByPage(cameraName,regionIdList);
   }

    public int videoState(Long intercomId) {
        int ret = 1;
        try {
            String url = videoUrl + "?videoIntercomId=" +  intercomId;
            String services = HttpClientUtils.getInstance().getUrl(url, null);
            JSONObject jsonObject = JSONObject.parseObject(services);
            Integer res =jsonObject.getInteger("data");
            ret = res == null ? 1 : res;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }
}
