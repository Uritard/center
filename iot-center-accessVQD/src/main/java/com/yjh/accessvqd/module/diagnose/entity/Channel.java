package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author czh
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "监测点对象", description = "视频诊断-监测点")
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

}
