package com.yjh.platform.module.patrol.entity.voice;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class ObjectData {
    private String objectId;//采集点位标识
    private List<String> typeList;//声纹分析类型 VoiceType
    private String voiceUrl;//待分析声纹的URL
    private String voiceCollectTime;//采集时间 格式为"yyyy-MM-dd HH:mm:ss"

}
