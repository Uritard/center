package com.yjh.accessvideo.module.control.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;

/**
 * @author hyh
 * @since 2022/4/11
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RecordFileInfo对象", description = "录制文件信息")
public class RecordFileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @Max(value= 999999999999999999L)
    @ApiModelProperty(value = "摄像头id")
    @TableField(value = "camera_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraId;

    @Max(value= 999999999999999999L)
    @ApiModelProperty(value = "上级区域ID")
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @Length(max = 255,message = "录制文件相对路径长度必须小于等于255")
    @ApiModelProperty(value = "录制文件相对路径")
    @TableField(value = "file_path",updateStrategy = FieldStrategy.IGNORED)
    private String filePath;

    @Length(max = 255,message = "录制文件绝对路径长度必须小于等于255")
    @ApiModelProperty(value = "录制文件绝对路径")
    @TableField(value = "absolute_file_path",updateStrategy = FieldStrategy.IGNORED)
    private String absoluteFilePath;

    @ApiModelProperty(value = "开始录制时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "start_time",updateStrategy = FieldStrategy.IGNORED)
    private Date startTime;

    @ApiModelProperty(value = "结束录制时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "end_time",updateStrategy = FieldStrategy.IGNORED)
    private Date endTime;

    @ApiModelProperty(value = "录制时长")
    private Long recordTimeValue;

    private String fileName;
}
