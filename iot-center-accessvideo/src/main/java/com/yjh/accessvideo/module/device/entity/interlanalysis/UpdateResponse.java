package com.yjh.accessvideo.module.device.entity.interlanalysis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 分析结果反馈接口参数
 *
 * @author zilong
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UpdateResponse {

    /**
     * 请求分析数据唯一标识，UUID
     */
    private String requestId;

    /**
     * 0 -更新失败
     * 1 -更新成功
     */
    private String result;
}
