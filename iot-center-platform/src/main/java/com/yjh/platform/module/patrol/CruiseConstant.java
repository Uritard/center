/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务常量
 *
 * @author Chenfei
 * @date 2022/10/28
 * @since [产品/模块版本] （可选）
 */
public interface CruiseConstant {

    /**
     * 巡视结果正常
     */
    int CRUISE_RESULT_NORMAL = 246;
    /**
     * 巡视结果异常
     */
    int CRUISE_RESULT_ABNORMAL = 247;

    /**
     * 审核状态，未审核
     */
    int EVALUATION_STATE_UN = 257;

    /**
     * 巡检数据状态，已执行
     */
    int CRUISE_STATE_DONE = 252;
    /**
     * 巡检数据状态，未执行
     */
    int CRUISE_STATE_UN = 253;
    /**
     * 巡检数据状态，执行失败
     */
    int CRUISE_STATE_FAILED = 254;

    /**
     * 异常原因，检修
     */
    int CRUISE_ABNORMAL_OVERHAUL = 410;
    /**
     * 异常原因，离线
     */
    int CRUISE_ABNORMAL_OFFLINE = 411;
    /**
     * 异常原因，抓图失败
     */
    int CRUISE_ABNORMAL_NOPIC = 248;

    /**
     * 任务状态，未开始
     */
    int TASK_STATE_UNSTART = 238;
    /**
     * 任务状态，正在执行
     */
    int TASK_STATE_RUNNING = 239;
    /**
     * 任务状态，执行完成
     */
    int TASK_STATE_DONE = 240;
    /**
     * 任务状态，暂停
     */
    int TASK_STATE_PAUSE = 241;
    /**
     * 任务状态，终止
     */
    int TASK_STATE_INTERRUPT = 242;
    /**
     * 任务状态，异常终止
     */
    int TASK_STATE_ABNORMAL = 243;

    enum TypeEnum {

        /**
         * 巡视类型，视频（可见光）
         */
        VIDEO(229),
        /**
         * 巡视类型，红外
         */
        INFRARED(230),
        /**
         * 巡视类型，声纹
         */
        VOICE(232),
        /**
         * 巡视类型，机器人
         */
        ROBOT(228),
        /**
         * 巡视类型，无人机
         */
        UAV(524),
        /**
         * 巡视类型，在线监控
         */
        ONLINE(231),
        /**
         * 其他类型，未识别类型
         */
        OTHERS(-1);

        final int code;

        private static final Map<Integer, TypeEnum> CRUISE_ENUM_MAP = new HashMap<>();

        static {
            for (TypeEnum value : TypeEnum.values()) {
                CRUISE_ENUM_MAP.put(value.getCode(), value);
            }
        }

        TypeEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TypeEnum getEnum(int code) {
            return CRUISE_ENUM_MAP.getOrDefault(code, TypeEnum.OTHERS);
        }
    }

    enum AnalyticsEnum {

        /**
         * 老的 tcp 协议
         */
        TCP,
        /**
         * 分析主机 http 协议
         */
        HTTP
    }
}
