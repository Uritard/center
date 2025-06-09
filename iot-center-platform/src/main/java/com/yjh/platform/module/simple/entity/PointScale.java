package com.yjh.platform.module.simple.entity;

import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/6
 * @since [产品/模块版本] （可选）
 */
@Data
public class PointScale {
    /**
     * 刻度级数，这里3就是有三级刻度（根据tier自动计算）
     */
    private int tiers;
    /**
     * 读数结果输出类型，0:浮点；1:整型；
     */
    private int dialInt;
    /**
     * 每级刻度级数的数量，这里就是一共有6个一级刻度，每两个一级之间有1个二级刻度，每两个二级刻度间有4个三级刻度；
     */
    private List<Integer> tier;
    /**
     * 指针的旋转中心点
     */
    private List<Double> shaftPos;
    /**
     * 标定时给出的刻度点，每一个刻度点有6个参数，分别是刻度，角度，刻度起点的x坐标，y坐标，刻度终点的x坐标，y坐标
     */
    private List<List<Double>> marks;
}
