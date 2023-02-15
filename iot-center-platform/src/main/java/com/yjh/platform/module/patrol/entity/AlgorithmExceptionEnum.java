package com.yjh.platform.module.patrol.entity;

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
    NO_TEMPLATE("No Template!", "缺少标定文件");




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

    public static AlgorithmExceptionEnum getInstance(String desc){
        for (AlgorithmExceptionEnum result : values()) {
            if (result.getDesc().equals(desc)) {
                return result;
            }
        }
        return null;
    }

    public static boolean isInclude(String content){
        boolean include = false;
        for (AlgorithmExceptionEnum e : AlgorithmExceptionEnum.values()){
            if(e.getContent().equals(content)){
                include = true;
                break;
            }
        }
        return include;
    }

}
