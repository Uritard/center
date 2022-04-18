package com.yjh.platform.module.task.scheduled;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * 请求图像分析参数
 *
 * @author zilong
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PicAnalyseRequest {
    /**
     * 分析结果反馈ip地址
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
     * 分析列表，JSON格式的数组. 支持多个巡视点位分析请求
     */
    private List<AnalyseObject> objectList;
}
