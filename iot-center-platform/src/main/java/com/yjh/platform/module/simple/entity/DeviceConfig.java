package com.yjh.platform.module.simple.entity;

import com.yjh.platform.module.patrol.entity.interlanalysis.OcrBox;
import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *  子点位配置信息
 *
 * @author huyuhang
 * @date 2025/6/6
 * @since [产品/模块版本] （可选）
 */
@Data
public class DeviceConfig {
    /**
     * 设备名称
     */
    private String devName;
    /**
     * 设备类型
     */
    private String devType;
    /**
     * 设备编号
     */
    private String devUuid;
    /**
     * 设备目标区域
     */
    private List<List<Integer>> tmplDevPos;
    /**
     * 指针类型的参数
     */
    private List<PointScale> pointScales;
    /**
     * 文字类型的参数
     */
    private List<OcrBox> ocrBoxes;
    /**
     * 指示灯类型的参数
     */
    private String cascadeType;
}
