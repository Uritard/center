package com.yjh.accessvideo.module.control.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author zilong
 * @since 2021-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("video_info")
@ApiModel(value="VideoInfo对象", description="")
public class VideoInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    @ApiModelProperty(value = "推流命令")
    private String command;

    @ApiModelProperty(value = "流地址")
    private String stream;

    @ApiModelProperty(value = "是否自启动")
    private Boolean enable;

    @ApiModelProperty(value = "是否回放")
    private Boolean back = false;
}
