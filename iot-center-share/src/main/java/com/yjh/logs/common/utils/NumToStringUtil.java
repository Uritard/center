package com.yjh.logs.common.utils;

/**
 * @author lqh
 * @since 2021/3/4
 */
public class NumToStringUtil {
    public static String findType(String str){
        if("保存".equals(str)){
            return "0";
        }
        if("查询".equals(str)){
            return "1";
        }
        if("新增".equals(str)){
            return "2";
        }
        if("修改".equals(str)){
            return "3";
        }
        if("删除".equals(str)){
            return "4";
        }
        if("确认".equals(str)){
            return "5";
        }
        if("登录".equals(str)){
            return "6";
        }
        if("登出".equals(str)){
            return "7";
        }
        if("导入".equals(str)){
            return "8";
        }
        if("导出".equals(str)){
            return "9";
        }
        if("任务下发".equals(str)){
            return "10";
        }
        if("任务暂停".equals(str)){
            return "11";
        }
        if("任务恢复".equals(str)){
            return "12";
        }
        if("任务终止".equals(str)){
            return "13";
        }
//        if("全部".equals(str)){
//            return "14";
//        }
//        if("查询".equals(str)){
//            return "15";
//        }





        if("0".equals(str)){
            return "保存";
        }
        if("1".equals(str)){
            return "查询";
        }
        if("2".equals(str)){
            return "新增";
        }
        if("3".equals(str)){
            return "修改";
        }
        if("4".equals(str)){
            return "删除";
        }
        if("5".equals(str)){
            return "确认";
        }
        if("6".equals(str)){
            return "登录";
        }
        if("7".equals(str)){
            return "登出";
        }
        if("8".equals(str)){
            return "导入";
        }
        if("9".equals(str)){
            return "导出";
        }
        if("10".equals(str)){
            return "任务下发";
        }
        if("11".equals(str)){
            return "任务暂停";
        }
        if("12".equals(str)){
            return "任务恢复";
        }
        if("13".equals(str)){
            return "任务终止";
        }
//        if("14".equals(str)){
//            return "全部";
//        }
        return null;
    }

}
