package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class CalibrationParams extends CalibrationBaseParams{
    /**
     * 刻度级数，这里3就是有三级刻度（根据tier自动计算）
     */
    private int tiers;
    /**
     * 读数结果输出类型，0:浮点；1:整型；
     */
    private int dialInt;
    /**
     * 指针读法：0：单指针; 1：双指针读单; 2：双指针读小; 3：双指针读大; 4：双指针组合;
     */
    private int ptrStyle;
    /**
     * 每级刻度级数的数量，这里就是一共有6个一级刻度，每两个一级之间有1个二级刻度，每两个二级刻度间有4个三级刻度；
     */
    private List<Integer> tier;
    /**
     * 指针的旋转中心点
     */
    private List<List<Double>> shaftPos;
    /**
     * 标定时给出的刻度点，每一个刻度点有6个参数，分别是刻度，角度，刻度起点的x坐标，y坐标，刻度终点的x坐标，y坐标
     */
    private List<List<Double>> marks;
    /**
     * 旋转后的坐标框
     */
    private RotateBox rotateBox;

    /**
     * 文字类型的参数
     */
    private String ocrType;

    /**
     * 旋钮文字信息
     */
    private String content;
}
