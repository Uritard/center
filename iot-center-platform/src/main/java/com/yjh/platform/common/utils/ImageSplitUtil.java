package com.yjh.platform.common.utils;

import cn.hutool.core.io.FileUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.configuration.SysParamConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/4
 * @since [产品/模块版本] （可选）
 */
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
    public static List<String> splitImage(File file, Integer splitNum, ImageSplitType type, String namePrefix) throws Exception {
        BufferedImage image = ImageIO.read(file);
        // 获取图片宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();
        // 计算每个分割部分
        int split;
        if (ImageSplitType.HORIZONTAL.equals(type)) {
            split = height / splitNum;
        } else if (ImageSplitType.VERTICAL.equals(type)) {
            split = width / splitNum;
        } else {
            split = height / splitNum;
        }
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < splitNum; i++) {
            BufferedImage splitImage;
            if (ImageSplitType.HORIZONTAL.equals(type)) {
                splitImage = image.getSubimage(0, i * split, width, split);
            } else if (ImageSplitType.VERTICAL.equals(type)) {
                splitImage = image.getSubimage(i * split, 0, split, height);
            } else {
                splitImage = image.getSubimage(0, i * split, width, split);
            }
            String splitPath = file.getParent() + File.separator + namePrefix + (i + 1) + ".jpg";
            File output = new File(splitPath);
            ImageIO.write(splitImage, "jpg", output);
            //路径转换
            paths.add(convertPath(splitPath, SysParamConfig.getSysContent("simplePicPath"), Constant.SIMPLE_PIC));
        }
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
