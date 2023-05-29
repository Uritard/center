package com.yjh.platform.module.patrol.entity;

import org.springframework.lang.NonNull;

/**
 * @author 丫C
 * @date 2023/2/14
 * 算法异常结果描述
 *
 * 待算法整理: 2001与2002的不一样
 */
public enum AlgorithmExceptionEnum {

    /**
     * 2001
     */
    NO_TEMPLATE("No Template!", "缺少标定文件"),

    /**
     * 2001
     */
    FAIL_DOWNLOAD_PICTURE("Faild to download image", "算法下载图片失败"),

    FAIL_DOWNLOAD_PB_PICTURE("Faild to download PB images ", "算法下载判别基准图失败"),

    FAIL_ANALYSE("Faild to analyse ", "算法分析失败");;




    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    String desc;
    String content;

    AlgorithmExceptionEnum(String desc, String content){
        this.content = content;
        this.desc = desc;
    }

    @NonNull
    public static AlgorithmExceptionEnum getInstance(String desc){
        for (AlgorithmExceptionEnum result : values()) {
            if (result.getDesc().equals(desc)) {
                return result;
            }
        }
        return FAIL_ANALYSE;
    }

    public static boolean isIncludeContent(String content){
        boolean include = false;
        for (AlgorithmExceptionEnum e : AlgorithmExceptionEnum.values()){
            if(e.getContent().equals(content)){
                include = true;
                break;
            }
        }
        return include;
    }

    public static boolean isIncludeDesc(String desc){
        boolean include = false;
        for (AlgorithmExceptionEnum e : AlgorithmExceptionEnum.values()){
            if(e.getDesc().equals(desc)){
                include = true;
                break;
            }
        }
        return include;
    }

    public static boolean isIncludeAny(String str){
        boolean include = false;
        for (AlgorithmExceptionEnum e : AlgorithmExceptionEnum.values()){
            if(e.getDesc().equals(str) || e.getContent().equals(str)){
                include = true;
                break;
            }
        }
        return include;
    }
}
