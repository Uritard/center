package com.yjh.platform.module.video.entity.cameralog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
@Data
@TableName(value = "t_camera_play_log")
public class CameraPlayLogDTO {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 相机id*
     */
    @TableField
    private Long cameraId;
    /**
     * 相机名称*
     */
    @TableField
    private String cameraName;
    /**
     * 用户id*
     */
    @TableField
    private Long userId;
    /**
     * 用户名*
     */
    @TableField
    private String userName;
    /**
     * 开始播放时间*
     */
    @TableField
    private String startTime;
    /**
     * 结束播放时间*
     */
    @TableField
    private String stopTime;
}
