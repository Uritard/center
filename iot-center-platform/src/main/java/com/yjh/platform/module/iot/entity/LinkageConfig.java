/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.iot.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class LinkageConfig {
    /**
     *
     */
    private Long id;
    /**
     * 设备类型 0-智能网关
     */
    private Integer type;

    /**
     * 网关ip
     */
    private String ip;
    /**
     * 备注解释
     */
    private String remark;
    /**
     * 区域编码
     */
    private String edgeCode;
    /**
     * 联动内容
     */
    private String data;

    private LinkageConfigResp.LinkageListData linkageListData;

}
