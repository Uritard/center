package com.yjh.accesstcp.common.utils;

/**
 * @author lqh
 * @since 2020/11/5
 */
public class MeteValueUtils {

    public static String meteValues(String commintValue){
        if("返回".equals(commintValue)){
            return "1";
        }
        if("启动".equals(commintValue)){
            return "0";
        }
        if("合".equals(commintValue)){
            return "1";
        }
        if("分".equals(commintValue)){
            return "0";
        }
        if("降".equals(commintValue)){
            return "1";
        }
        if("升".equals(commintValue)){
            return "0";
        }
        if("停".equals(commintValue)){
            return "2";
        }
        if("投入".equals(commintValue)){
            return "1";
        }
        if("退出".equals(commintValue)){
            return "0";
        }
        if("控合".equals(commintValue)){
            return "1";
        }
        if("控分".equals(commintValue)){
            return "0";
        }
        if("未储能".equals(commintValue)){
            return "1";
        }
        if("已储能".equals(commintValue)){
            return "0";
        }
        if("联锁".equals(commintValue)){
            return "1";
        }
        if("解锁".equals(commintValue)){
            return "0";
        }
        if("成功".equals(commintValue)){
            return "1";
        }
        if("失败".equals(commintValue)){
            return "0";
        }
        if("远方".equals(commintValue)){
            return "1";
        }
        if("本地".equals(commintValue)){
            return "0";
        }
        if("合上".equals(commintValue)){
            return "1";
        }
        if("断开".equals(commintValue)){
            return "0";
        }
        if("中断".equals(commintValue)){
            return "1";
        }
        if("恢复".equals(commintValue)){
            return "0";
        }
        if("复归".equals(commintValue)){
            return "2";
        }
        if("上限".equals(commintValue)){
            return "1";
        }
        if("下限".equals(commintValue)){
            return "0";
        }
        if("触发".equals(commintValue)){
            return "2";
        }
        if("故障".equals(commintValue)){
            return "0";
        }
        if("非电量".equals(commintValue)){
            return "0";
        }
        if("保护跳闸".equals(commintValue)){
            return "0";
        }
        if("火灾报警".equals(commintValue)){
            return "0";
        }
        if("报警".equals(commintValue)){
            return "0";
        }
        if("门开".equals(commintValue)){
            return "1";
        }
        if("门关".equals(commintValue)){
            return "0";
        }
        return null;
    }
}
