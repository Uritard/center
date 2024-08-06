package com.yjh.platform.module.patrol.entity.voice;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

/**
 * 声纹http接口-请求声纹分析
 *
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class VoicePrintAnalyseReq {

    private String requestHostIp;//采集数据上传ip地址 巡视主机ip
    private String requestHostPort;//上传数据上传端口 巡视主机端口
    private String requestId;//请求采集数据唯一标识， UUID
    private List<ObjectData> objectList;//采集列表， JSON格式的数组 内容：ObjectData

}
