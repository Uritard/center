/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode
public class RespSource {

    @Schema(description = "请求返回ip地址")
    private String requestHostIp;

    @Schema(description = "请求返回端口")
    private String requestHostPort;

    @Schema(description = "请求数据唯一标识，UUID")
    private String requestId;

    public RespSource() {

    }

    public RespSource(String requestHostIp, String requestHostPort, String requestId) {
        this.requestHostIp = requestHostIp;
        this.requestHostPort = requestHostPort;
        this.requestId = requestId;
    }
}
