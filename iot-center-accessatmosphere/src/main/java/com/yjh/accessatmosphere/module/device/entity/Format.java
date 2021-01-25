package com.yjh.accessatmosphere.module.device.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @author tt
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Format对象", description = "测试表")
public class Format implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "主键ID")
    @TableField(value = "test_id",updateStrategy = FieldStrategy.IGNORED)
    private String testId;

    @ApiModelProperty(value = "分类标志")
    @TableField(value = "test_type",updateStrategy = FieldStrategy.IGNORED)
    private String testType;


}
