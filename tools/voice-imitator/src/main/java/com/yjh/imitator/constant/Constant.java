package com.yjh.imitator.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/13
 * @since [产品/模块版本] （可选）
 */
public interface Constant {
    int CPU_NUM = Runtime.getRuntime().availableProcessors();

    String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    String ROOT_DIR = "/home/webapp/files/";

    String FILES_PREFIX = "/voice-files/";

    enum VoiceDefect{
        /**
         *
         */
        PD("局部放电"),
        SUSPEND_PD("悬浮放电"),
        SURFACE_PD("沿面放电"),
        CORONA_PD("电晕放电"),
        SUSPEND_SURFACE_PD("悬浮，悬浮放电"),
        DHFD("电弧放电"),
        BYQ_ZLPC("变压器直流偏磁"),
        BYQ_ZGZ("变压器重过载"),
        BYQ_DLCJ("变压器短路冲击"),
        BYQ_ZBJ_LOOSENESS("变压器组部件松动"),
        BYQ_LQQYX("变压器冷却器异响");

        private String desc;
        VoiceDefect(String desc){
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
