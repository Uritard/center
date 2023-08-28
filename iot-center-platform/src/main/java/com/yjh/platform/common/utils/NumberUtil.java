package com.yjh.platform.common.utils;

import java.util.Random;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/8/23
 * @since [产品/模块版本] （可选）
 */
public class NumberUtil {

    /**
     * 获取指定范围的int类型随机数
     *
     * @param min 随机数的最小值
     * @param max 随机数的最大值
     * @return 随机数
     */
    public static int getIntRandomNum(int min, int max) {
        // 创建Random对象
        Random random = new Random();
        return (int) (min + (max - min) * random.nextDouble());
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println("随机数：" + getIntRandomNum(0, 10000));
        }
    }
}
