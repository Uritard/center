package com.yjh.platform.common.utils;

import com.yjh.platform.common.Constant;
import com.yjh.platform.configuration.SysParamConfig;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/4
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class ImageSplitUtil {

    /**
     * 图片分割
     *
     * @param file       图片文件
     * @param splitNum   分割数量
     * @param type       分割类型
     * @param namePrefix 图片名称前缀
     * @return 分割后的图片相对路径
     * @throws Exception 抛出异常
     */
    public static String[] splitImage(File file, Integer splitNum, ImageSplitType type, String namePrefix) throws Exception {
        BufferedImage image = ImageIO.read(file);
        // 获取图片宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();
        // 计算每个分割部分
        int split;
        boolean isHorizontal = ImageSplitType.HORIZONTAL.equals(type);
        boolean isVertical = ImageSplitType.VERTICAL.equals(type);
        if (isHorizontal) {
            split = height / splitNum;
        } else {
            split = width / splitNum;
        }
        String[] paths = new String[splitNum];
        CountDownLatch latch = new CountDownLatch(splitNum);
        for (int i = 0; i < splitNum; i++) {
            final int index = i;
            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                try {
                    int x = isVertical ? index * split : 0;
                    int y = isHorizontal ? index * split : 0;
                    int subWidth = isVertical ? split : width;
                    int subHeight = isHorizontal ? split : height;

                    BufferedImage splitImage = image.getSubimage(x, y, subWidth, subHeight);

                    // 使用 Paths 构建路径，提升可读性和兼容性
                    String formatName = "jpg";
                    Path splitPath = Paths.get(file.getParent(), namePrefix + (index + 1) + "." + formatName);
                    File output = splitPath.toFile();

                    ImageIO.write(splitImage, formatName, output);
                    paths[index] = convertPath(splitPath.toString(), SysParamConfig.getSysContent("simplePicPath"), Constant.SIMPLE_PIC);
                } catch (Exception e) {
                    log.error("图片分割失败", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); // 等待所有线程执行完毕
        return paths;
    }
    /**
     * 将相对路径转换为绝对路径
     */
    public static String convertPath(String originalPath, String fromPrefix, String toPrefix) {
        return originalPath.replaceFirst(Pattern.quote(fromPrefix), Matcher.quoteReplacement(toPrefix));
    }

    public enum ImageSplitType {
        /**
         * 垂直
         */
        VERTICAL,
        /**
         * 水平
         */
        HORIZONTAL
    }

}
