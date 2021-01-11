package com.yjh.accessatmosphere.module.device.entity;

import java.io.Serializable;
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

    @NotEmpty(message = "Id不为空")
    @Length(min=1,max = 10,message = "Id长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "主键ID")
    private String testId;
    @Length(min=1,max = 10,message = "testType长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "分类标志")
    private String testType;


}
