package com.yjh.platform.module.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogDTO;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogQueryReq;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
@Repository
public interface CameraPlayLogDAO extends BaseMapper<CameraPlayLogDTO> {
}
