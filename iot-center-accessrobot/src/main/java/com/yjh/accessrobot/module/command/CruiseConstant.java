package com.yjh.accessrobot.module.command;


/**
 * 常量值
 * @author YJH
 */
public interface CruiseConstant {

    /**
     * 巡视类型，全面巡视
     */
    int TOTAL_PATROL = 213;
    /**
     * 巡视类型，例行巡视
     */
    int ROUTINE_PATROL = 214;
    /**
     * 巡视类型，熄灯巡视
     */
    int LIGHTS_OUT_PATROL = 215;
    /**
     * 巡视类型，特殊巡视
     */
    int SPECIAL_PATROL = 216;
    /**
     * 巡视类型，专项巡视
     */
    int SPECIAL_PROJECT_PATROL = 217;
    /**
     * 巡视类型，自定义巡视
     */
    int CUSTOM_PATROL = 218;
    /**
     * 巡视类型，操作-操作票
     */
    int OPERATION_ORDER_PATROL = 456;
    /**
     * 巡视类型，操作-单设备
     */
    int SINGLE_DEVICE_PATROL = 508;
    /**
     * 巡视类型，操作-紧急分合闸
     */
    int EMERGENCY_PATROL = 509;

    enum CruiseTypeEnum{
        /**
         * 正常任务
         */
        NORMAL_TASK("101"),
        /**
         * 联动任务
         */
        LINKAGE_TASK("102");

        public String getType() {
            return type;
        }

        final String type;

        CruiseTypeEnum(String type) {this.type = type;}
    }
}
