package com.yjh.accessudp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/11/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "THisTelemeterDate对象", description = "遥测历史数据表")
public class THisTelemeterData extends TCfgDataCurrent{
}

