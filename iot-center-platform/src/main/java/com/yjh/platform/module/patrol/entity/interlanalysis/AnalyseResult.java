package com.yjh.platform.module.patrol.entity.interlanalysis;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分析对象定义
 *
 * @author zilong
 * @date 2022/4/11
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AnalyseResult {
    /**
     * 分析点位标识(巡视点位ID)
     */
    private String objectId;

    /**
     * 分析结果
     */
    private List<AnalyseResultItem> results;
}
