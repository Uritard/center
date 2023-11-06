package com.yjh.platform.module.video.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.video.dao.CameraPlayLogDAO;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogDTO;
import com.yjh.platform.module.video.service.CameraPlayLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
@Service
public class CameraPlayLogServiceImpl extends ServiceImpl<CameraPlayLogDAO, CameraPlayLogDTO> implements CameraPlayLogService {
    @Resource
    private CameraPlayLogDAO cameraPlayLogDAO;
    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private TCameraInfoDao tCameraInfoDao;

    @Override
    public List<CameraPlayLogDTO> queryCameraPlayLog(CameraPlayLogDTO cameraPlayLogDTO) {
        List<CameraPlayLogDTO> dtoList;
        dtoList = cameraPlayLogDAO.selectList(Wrappers.<CameraPlayLogDTO>lambdaQuery()
                        .eq(cameraPlayLogDTO.getCameraId() != null, CameraPlayLogDTO::getCameraId, cameraPlayLogDTO.getCameraId())
                        .eq(cameraPlayLogDTO.getUserId() != null, CameraPlayLogDTO::getUserId, cameraPlayLogDTO.getUserId())
                        .like(StringUtils.isNotBlank(cameraPlayLogDTO.getUserName()), CameraPlayLogDTO::getUserName, cameraPlayLogDTO.getUserName())
                        .orderByDesc(CameraPlayLogDTO::getStartTime)
                );
        return dtoList;
    }

    @Override
    public void updateStopLog(CameraPlayLogDTO cameraPlayLogDTO) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        super.update().set("stop_time", currentTime)
                .eq("camera_id", cameraPlayLogDTO.getCameraId())
                .eq("user_id", cameraPlayLogDTO.getUserId())
                .isNull("stop_time").update();
    }

    @Override
    public void addStartLog(CameraPlayLogDTO cameraPlayLogDTO) {
        Asserts.notNull(cameraPlayLogDTO.getCameraId(), "相机id缺失");
        Asserts.notNull(cameraPlayLogDTO.getUserId(), "用户id缺失");
        SysUser sysUser = sysUserDao.selectByPrimaryId(cameraPlayLogDTO.getUserId());
        cameraPlayLogDTO.setUserName(sysUser.getUserName());
        TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(cameraPlayLogDTO.getCameraId());
        cameraPlayLogDTO.setCameraName(tCameraInfo.getCameraName());
        cameraPlayLogDTO.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cameraPlayLogDAO.insert(cameraPlayLogDTO);
    }
}
