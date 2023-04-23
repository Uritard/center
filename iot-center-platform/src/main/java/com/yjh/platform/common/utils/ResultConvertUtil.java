package com.yjh.platform.common.utils;

import com.yjh.platform.module.patrol.entity.interlanalysis.RecogniseStatusEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/4/21
 * @since [产品/模块版本] （可选）
 */
public class ResultConvertUtil {

    private static final String[] ARR = {"_", "分", "合"};

    public static void main(String[] args) {
        String a = " 表计表盘模糊 ";
        String b = " 挂空悬浮物  鸟巢  鸟巢 ";
        String c = "分";
        String d = "分_合_合_合_合_合_合";
        System.out.println(convertResult(a));
        System.out.println(convertResult(b));
        System.out.println(convertResult(c));
        System.out.println(convertResult(d));
    }

    /**
     * 结果转换为数值
     *
     * @param result 结果
     * @return resultNum 目标值
     */
    public static String convertResult(String result) {
        RecogniseStatusEnum statusEnum = RecogniseStatusEnum.getCodeByValue(result);
        if (Objects.nonNull(statusEnum)) {
            String code = String.valueOf(statusEnum.getCode());
            return StringUtils.substring(code, code.length() - 1, code.length());
        } else if (StringUtils.containsAny(result, ARR)) {
            return String.valueOf(result.split("_").length);
        } else if (StringUtils.startsWith(result, StringUtils.SPACE) && StringUtils.endsWith(result, StringUtils.SPACE)) {
            return String.valueOf(result.split(" {2}").length);
        } else {
            return result;
        }
    }
}
