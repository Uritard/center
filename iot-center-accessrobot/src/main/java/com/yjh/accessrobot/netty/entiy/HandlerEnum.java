package com.yjh.accessrobot.netty.entiy;

/**
 * @author YChen
 * @date 2021/12/14
 */
public enum HandlerEnum {
    // 注册指令
    REGISTER("2511","register"),
    // 心跳指令
    HEART_BEAT("2512","heartBeat"),
    // 模型同步和任务控制
    MODEL_SYNC("2514","modelSync"),
    // 机器人控制和任务下发
    TASK_CONTROLLER("2513","taskController"),
    // 机器人状态数据
    ROBOT_STATUS("1","robotStatus"),
    // 机器人运行数据
    ROBOT_RUN_DATA("2","robotRunData"),
    // 机器人坐标
    ROBOT_COORDINATE("3","robotCoordinate"),
    // 机器人巡视路线
    ROBOT_PATROL_ROUTE("4","robotPatrolRoute"),
    // 机器人异常告警数据
    ROBOT_WARN("5","robotWarn"),
    // 微气象数据
    MICRO_WEATHER_DATA("21","microWeatherData"),
    // 任务状态数据
    ROBOT_TASK_STATUS("41","robotTaskStatus"),
    // 巡视结果
    INSPECTION_RESULT("61","inspectionResult"),
    // 机器人站端任务
    ROBOT_SELF_TASK("71","robotSelfTask"),
    // 机器人设备测点告警数据
    ROBOT_INSPECTION_WARN("62","robotInspectionWarn");

    /**
     * 状态值
     */
    private String code;
    /**
     * 类型描述
     */
    private String value;

    private HandlerEnum(String code, String value){
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public static HandlerEnum getInstance(String code){
        for (HandlerEnum handlerNum : values()) {
            if (handlerNum.getCode().equals(code)) {
                return handlerNum;
            }
        }
        return null;
    }

}
