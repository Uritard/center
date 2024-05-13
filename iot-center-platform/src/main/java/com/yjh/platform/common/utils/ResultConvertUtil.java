package com.yjh.platform.common.utils;

import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.AlgorithmExceptionEnum;
import com.yjh.platform.module.patrol.entity.interlanalysis.RecogniseStatusEnum;
import com.yjh.platform.module.patrol.entity.interlanalysis.RecogniseStatusExEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/4/21
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class ResultConvertUtil {

    public static final String[] ARR = {"_", "分", "合"};
    public static final String FIFTY = "50";

    public static void main(String[] args) {
//        String a = " 表计表盘模糊 ";
       String b = " 挂空悬浮物,鸟巢,鸟巢    鸟巢,meibn, ";
//        String c = "分";
       String d = "分_合_合_合_合 合 合";
//        System.out.println(convertResult(a));
//        System.out.println(convertResult(b));
//        System.out.println(convertResult(c));
       System.out.println(convertQXResult(b));
        System.out.println(convertResult("机器人任务异常"));
        System.out.println(convertBJResult("32.6ABCDEFGHIJKL,MOPQRSTUVWXYZa我-1"));
    }

    /**
     * 结果转换为数值
     *
     * @param result 结果
     * @return resultNum 目标值
     */
    public static String convertResult(String result) {
        if (StringUtils.isBlank(result)) {
            return "0";
        }
        RecogniseStatusEnum statusEnum = RecogniseStatusEnum.getCodeByValue(result);
        RecogniseStatusExEnum statusExEnum = RecogniseStatusExEnum.getCodeByValue(result);
        if (Objects.nonNull(statusEnum)) {
            String code = String.valueOf(statusEnum.getCode());
            return StringUtils.substring(code, code.length() - 1, code.length());
        } else if (Objects.nonNull(statusExEnum)) {
            String code = String.valueOf(statusExEnum.getCode());
            return StringUtils.substring(code, code.length() - 1, code.length());
        } else if (StringUtils.containsAny(result, ARR)) {
            return String.valueOf(StringUtils.split(result,"_ ").length);
        } else if (StringUtils.containsAny(result, "正常", "未见异常")) {
            return "0";
        } else if (StringUtils.containsAny(result, "异常", "超时", "错误", "失败", "未识别")) {
            return "-1";
        } else if (CommonUtils.containsChinese(result)) {
            return String.valueOf(StringUtils.split(result).length);
        } else {
            return CommonUtils.getNumberStr(result);
        }
    }

    /**
     * D200 判断结果是不是放电
     * @param filepath 局放 .txt文件路径
     * @return 只返回放电情况
     */
    public static Pair<String, String> dealJudgment(String filepath, String val, String unit) {
        File file = new File(filepath);
        Pair<String, String> res;
        if (file.exists()) {
            List<String[]> list;
            try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
                list = br.lines().map(s -> s.split(",")).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("readFileList error", e);
                return Pair.of("-1", "图谱文件异常");
            }
            if (list.size() == 0) {
                log.info("图谱文件data.txt大小为0");
                return Pair.of("-1", "图谱文件异常");
            }
            String[] result = list.get(list.size() - 1);
            String type = result[result.length - 2];
            log.info("局放数据：" + type);
            String value;
            switch (type) {
                case "1":
                    value = "内部放电";
                    break;
                case "2":
                    value = "表面放电";
                    break;
                case "3":
                    value = "悬浮电位";
                    break;
                case "4":
                    value = "电晕放电";
                    break;
                case "9":
                    value = "未采集完";
                    break;
                default:
                    value = "非放电";
                    type = "0";
                    break;
            }
            res = Pair.of(type, value + "（" + val + unit + "）");
        } else {
            log.info("图谱文件data.txt不存在");
            res = Pair.of(val, val + unit);
        }
        return res;
    }

    /**
     * 结果转换为带单位数值
     * @param value 结果
     * @param unit 单位
     */
    public static String convertDesc(String value, String unit) {
        if (CommonUtils.isEmptyOrNullstr(value)) {
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
        if (StringUtils.containsAny(result, "正常", "未见异常")) {
            return "0";
        }
        if (StringUtils.isNotEmpty(result)){
            return String.valueOf(StringUtils.split(result, ", ").length);
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
                } else if (StringUtils.containsAny(result, ARR)) {
                    return String.valueOf(StringUtils.split(result,"_ ").length);
                } else if (StringUtils.containsAny(result, "正常", "未见异常")) {
                    return "0";
                } else if (StringUtils.contains(result, "任务异常")) {
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
