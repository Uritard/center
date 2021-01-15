package com.yjh.accessvqd.module.diagnose.entity;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author czh
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "诊断结果对象", description = "视频诊断-诊断结果")
public class ChanResult implements Serializable {

    private static final long serialVersionUID = 1L;
}
