package com.yjh.platform.module.video.convert;

import cn.hutool.core.bean.BeanUtil;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogDTO;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogVO;
import org.apache.http.util.Asserts;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
public class CameraPlayLogConverter {
    public static CameraPlayLogVO convertDTO2VO(CameraPlayLogDTO cameraPlayLogDTO) {
        if (cameraPlayLogDTO != null) {
            CameraPlayLogVO cameraPlayLogVO = new CameraPlayLogVO();
            BeanUtil.copyProperties(cameraPlayLogDTO, cameraPlayLogVO);
            if (cameraPlayLogDTO.getStopTime() != null) {
                // 计算时长
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date startTime = format.parse(cameraPlayLogDTO.getStartTime());
                    Date stopTime = format.parse(cameraPlayLogDTO.getStopTime());
                    long diffInMilliseconds = Math.abs(stopTime.getTime() - startTime.getTime());
                    long diffHours = TimeUnit.MILLISECONDS.toHours(diffInMilliseconds);
                    long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliseconds) % 60;
                    long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMilliseconds) % 60;
                    cameraPlayLogVO.setPlayDuration(diffHours + "小时" + diffMinutes + "分钟" + diffSeconds + "秒");
                } catch (Exception e) {
                    throw new BusinessException("计算时长异常");
                }
            }
            return cameraPlayLogVO;
        }
        return null;
    }

    public static CameraPlayLogDTO convertVO2DTO(CameraPlayLogVO cameraPlayLogVO) {
        Asserts.notNull(cameraPlayLogVO, "参数异常");
        CameraPlayLogDTO cameraPlayLogDTO = new CameraPlayLogDTO();
        BeanUtil.copyProperties(cameraPlayLogVO, cameraPlayLogDTO);
        return cameraPlayLogDTO;
    }
}
