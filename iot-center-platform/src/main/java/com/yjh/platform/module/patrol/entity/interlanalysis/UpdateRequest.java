package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 请求图像分析参数
 *
 * @author zilong
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UpdateRequest {
    /**
     * 请求图像分析参数
     */
    private String requestHostIp;

    /**
     * 分析结果返回端口
     */
    private String requestHostPort;

    /**
     * 请求分析数据唯一标识，UUID
     */
    private String requestId;

    /**
     * 待获取更新算法路径(待获取更新算法路径)
     */
    private String algorithmPath;
}
