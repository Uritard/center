package com.yjh.accessvideo.module.device.entity.interlanalysis;

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
public class AnalyseResultItem {
    /**
     * 分析类型
     */
    private String type;

    /**
     * 值
     */
    private String value;

    /**
     * 正确 2000; 图像数据错误 2001; 算法分析失败 2002
     * 图像数据错误是指未能获取到图像数据；
     * 算法分析失败是指算法本身分析过程中出错
     */
    private String code;

    /**
     * 结果反馈图像 url 路径
     */
    private String resImageUrl;

    /**
     * 图中区域，按照顺时针顺序提供的一系列坐标列表
     * 支持多个区域框，支持多边形和矩形框
     */
    private List<Area> pos;

    /**
     * 分析结果置信度. 范围 0-1，保留4位小数
     */
    private float conf;

    /**
     * 分析结果描述
     */
    private String desc;


}
