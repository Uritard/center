package com.yjh.platform.module.user.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

/**
 * @author lqh
 * @since 2020-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraGroup对象", description = "相机分组表")
public class TCameraGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "分组ID")
    @TableField(value = "group_id",updateStrategy = FieldStrategy.IGNORED)
    private Long groupId;

    @Length(max = 128,message = "groupName长度必须小于等于128")
    @ApiModelProperty(value = "分组名称")
     @TableField(value = "group_name",updateStrategy = FieldStrategy.IGNORED)
    private String groupName;

    @Length(max = 255,message = "cameraIds长度必须小于等于255")
    @ApiModelProperty(value = "相机ID")
    private String cameraIds;

    @Length(max = 64,message = "remarks长度必须小于等于64")
    @ApiModelProperty(value = "备注")
    @TableField(value = "remarks",updateStrategy = FieldStrategy.IGNORED)
    private String remarks;


}
