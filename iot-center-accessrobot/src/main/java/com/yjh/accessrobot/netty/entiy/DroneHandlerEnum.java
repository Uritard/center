package com.yjh.accessrobot.netty.entiy;

/**
 * @author YChen
 * @date 2021/12/14
 */
public enum DroneHandlerEnum {
    // 注册指令
    REGISTER("2511","register"),
    // 心跳指令
    HEART_BEAT("2512","heartBeat"),
    // 模型同步和任务控制
    MODEL_SYNC("2514","modelSync"),
    // 无人机控制和任务下发
    TASK_CONTROLLER("1011","taskController"),
    // 无人机状态数据
    DRONE_STATUS("1","droneStatus"),
    // 无人机运行数据
    DRONE_RUN_DATA("2","droneRunData"),
    // 无人机机巢状态数据
    NEST_STATUS("20001","droneStatus"),
    // 无人机机巢运行数据
    NEST_RUN_DATA("10004","droneRunData"),
    // 无人机坐标
    DRONE_COORDINATE("3","droneCoordinate"),
    // 无人机巡视路线
    DRONE_PATROL_ROUTE("4","dronePatrolRoute"),
    // 无人机异常告警数据
    DRONE_WARN("5","droneWarn"),
    // 微气象数据
    MICRO_WEATHER_DATA("21","microWeatherData"),
    // 任务状态数据
    DRONE_TASK_STATUS("41","droneTaskStatus"),
    // 巡视结果
    INSPECTION_RESULT("61","inspectionResult"),
    // 无人机站端任务
    DRONE_SELF_TASK("71","droneSelfTask"),
    // 无人机设备测点告警数据
    DRONE_INSPECTION_WARN("62","droneInspectionWarn"),
    //环境数据异常告警数据
    ENV_WARN("22","envWarn"),
    //机器操作结果
    OPERATION_RESULT("64","OperationResult"),
    //无人机确认消息
    DRONE_CONFIRM_MSG("81","droneConfirmMsg"),
    //操作步骤消息
    OPERATION_STEPS("82","droneOperationSteps");

    /**
     * 状态值
     */
    private String code;
    /**
     * 类型描述
     */
    private String value;

    private DroneHandlerEnum(String code, String value){
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public static DroneHandlerEnum getInstance(String code){
        for (DroneHandlerEnum handlerNum : values()) {
            if (handlerNum.getCode().equals(code)) {
                return handlerNum;
            }
        }
        return null;
    }

}
