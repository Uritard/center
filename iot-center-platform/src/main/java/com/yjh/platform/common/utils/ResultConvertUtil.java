package com.yjh.platform.common.utils;

import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.AlgorithmExceptionEnum;
import com.yjh.platform.module.patrol.entity.interlanalysis.RecogniseStatusEnum;
import com.yjh.platform.module.patrol.entity.interlanalysis.RecogniseStatusExEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
//        String a = " 表计表盘模糊 ";
//        String b = " 挂空悬浮物  鸟巢  鸟巢 ";
//        String c = "分";
//        String d = "分_合_合_合_合_合_合";
//        System.out.println(convertResult(a));
//        System.out.println(convertResult(b));
//        System.out.println(convertResult(c));
//        System.out.println(convertResult(d));
        System.out.println(convertResult("机器人任务异常"));
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
            return String.valueOf(StringUtils.split(result,"_").length);
        } else if (StringUtils.contains(result, "正常")) {
            return "0";
        } else if (StringUtils.contains(result, "异常")) {
            return "-1";
        }else if (CommonUtils.containsChinese(result)) {
            return String.valueOf(StringUtils.split(result).length);
        } else {
            return CommonUtils.getNumberStr(result);
        }
    }

    /**
     * 结果转换为带单位数值
     * @param value 结果
     * @param unit 单位
     */
    public static String convertDesc(String value, String unit) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        if (CommonUtils.containsChinese(value)) {
            return value;
        }
        String unitLe = unit == null ? "" : unit;
        String[] resultStrings = StringUtils.split(value,",");
        StringBuilder retDesc = new StringBuilder();
        for (String val : resultStrings) {
            retDesc.append(val).append(unitLe).append(",");
        }
        CommonUtils.clearLastChar(retDesc);
        return retDesc.toString();
    }

    /**
     * 审核结果转换为数值
     *
     * @param checkResult 结果
     * @return resultNum 目标值
     */
    public static String convertResult(String checkResult,String type) {
        if (AlgorithmExceptionEnum.isIncludeAny(checkResult) || CruiseConstant.AbnormalResDescEnum.contains(checkResult)) {
            return "-1";
        }

        switch (type){
            case "表计":
                return convertBJResult(checkResult);
            case "缺陷":
                return convertQXResult(checkResult);
            case "判别":
                return convertPBResult(checkResult);
            default:break;
        }
        return "0";
    }

    /**
     * 缺陷结果转换为数值
     *
     * @param result 结果
     * @return resultNum 目标值
     */
    public static String convertQXResult(String result) {
        if (StringUtils.isNotEmpty(result)){
            result = result.replace(","," ")
                    .replace("，"," ");
            return String.valueOf(result.split(" ").length);
        }
        return "0";
    }

    /**
     * 表计结果转换为数值
     *
     * @param result 结果
     * @return resultNum 目标值
     */
    public static String convertBJResult(String result) {
        if (StringUtils.isNotEmpty(result)){
            if (CommonUtils.containsNumber(result)){
                return CommonUtils.getNumberStr(result);
            } else {
                //汉字
                RecogniseStatusEnum statusEnum = RecogniseStatusEnum.getCodeByValue(result);
                RecogniseStatusExEnum statusExEnum = RecogniseStatusExEnum.getCodeByValue(result);
                if (Objects.nonNull(statusEnum)) {
                    String code = String.valueOf(statusEnum.getCode());
                    return StringUtils.substring(code, code.length() - 1, code.length());
                } else if (Objects.nonNull(statusExEnum)) {
                    String code = String.valueOf(statusExEnum.getCode());
                    return StringUtils.substring(code, code.length() - 1, code.length());
                } else if (StringUtils.contains(result, "正常")) {
                    return "0";
                } else if (StringUtils.contains(result, "异常")) {
                    return "-1";
                }else {
                    return "0";
                }
            }
        }
        return "0";
    }

    /**
     * 判别结果转换为数值
     *
     * @param result 结果
     * @return resultNum 目标值
     */
    public static String convertPBResult(String result) {
        if (StringUtils.isNotEmpty(result)){
            if (!result.matches("\\.*\\d+.*")){
                //汉字 有差异 无差异
                if (result.contains("有")) {
                    return "1";
                } else {
                    return "0";
                }
            } else {
                return result;
            }
        }
        return "0";
    }
}
