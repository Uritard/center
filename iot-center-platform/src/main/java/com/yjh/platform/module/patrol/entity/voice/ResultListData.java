package com.yjh.platform.module.patrol.entity.voice;

import lombok.Data;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class ResultListData {

    private String objectId;

    private String code;

    private List<ResultData> results;
}
