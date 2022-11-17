package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 分析结果反馈接口参数
 *
 * @author zilong
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PicAnalyseResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 请求分析数据唯一标识，UUID
     */
    private String requestId;

    /**
     * 结果集
     */
    private List<AnalyseResult> resultsList;
}
