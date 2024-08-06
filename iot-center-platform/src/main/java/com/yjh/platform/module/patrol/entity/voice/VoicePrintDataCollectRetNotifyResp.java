package com.yjh.platform.module.patrol.entity.voice;

import lombok.Data;

import java.util.List;

/**
 *声纹http接口-声纹监测装置返回声纹采集数据
 *
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class VoicePrintDataCollectRetNotifyResp {

    private String requestId;

    private List<ResultListData> resultList;

}
