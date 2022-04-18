package com.yjh.platform.module.task.scheduled;

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
public class AnalyseObject {
    /**
     * 分析点位标识(巡视点位ID)
     */
    private String objectId;

    /**
     * 图像分析类型(定义见N.3.5)
     */
    private List<String> typeList;
    /**
     * 判别基准图(用于判别模板)。
     * 可选
     */
    private String imageNormalUrlPath;
    /**
     * 待分析图像的URL(支持多张图像)
     */
    private List<String> imageUrlList;
}
