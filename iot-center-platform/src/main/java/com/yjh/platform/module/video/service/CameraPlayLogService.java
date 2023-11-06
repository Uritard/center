package com.yjh.platform.module.video.service;

import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogDTO;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogQueryReq;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogVO;

import java.util.List;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
public interface CameraPlayLogService {
    /**
     * 分页查询相机播放记录*
     * @param CameraPlayLogDTO
     * @return
     */
    List<CameraPlayLogDTO> queryCameraPlayLog(CameraPlayLogDTO CameraPlayLogDTO);

    /**
     * 更新播放记录*
     * @param cameraPlayLogDTO
     */
    void updateStopLog(CameraPlayLogDTO cameraPlayLogDTO);

    /**
     * 新增播放记录*
     * @param cameraPlayLogDTO
     */
    void addStartLog(CameraPlayLogDTO cameraPlayLogDTO);


}
