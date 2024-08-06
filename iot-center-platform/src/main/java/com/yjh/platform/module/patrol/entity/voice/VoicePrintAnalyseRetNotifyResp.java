package com.yjh.platform.module.patrol.entity.voice;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

/**
 * 声纹http接口-声纹分析结果反馈
 *
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class VoicePrintAnalyseRetNotifyResp {

    private String requestId;//采集数据上传ip地址 巡视主机ip
    List<ResultListData> resultList;

}
