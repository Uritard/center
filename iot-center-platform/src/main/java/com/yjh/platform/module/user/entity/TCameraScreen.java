package com.yjh.platform.module.user.entity;

import java.util.Date;
import java.io.Serializable;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 * @author lqh
 * @since 2020-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraScreen对象", description = "分屏配置表")
public class TCameraScreen implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableField(value = "user_id",updateStrategy = FieldStrategy.IGNORED)
    private Long userId;

    @Length(max = 50,message = "screenNum长度必须小于等于50")
    @TableField(value = "screen_num",updateStrategy = FieldStrategy.IGNORED)
    private String screenNum;

    @Length(max = 200,message = "cameraIds长度必须小于等于200")
    @TableField(value = "camera_ids",updateStrategy = FieldStrategy.IGNORED)
    private String cameraIds;

    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
