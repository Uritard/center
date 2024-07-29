package com.yjh.accessrobot.netty.thread;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.EdgeEnum;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/3/24
 * @since [产品/模块版本] （可选）
 */
public class TaskShutDownThread implements Runnable{

    private final String taskId;
    private final String errorCode;
    private final RobotService robotService;

    public TaskShutDownThread(RobotService robotService, String taskId, String errorCode){
        this.robotService = robotService;
        this.taskId = taskId;
        this.errorCode = errorCode;
    }

    @Override
    public void run() {
        if (Optional.ofNullable(taskId).isPresent()){
            Map<String, Object> params = new HashMap<>(4);
            params.put("taskId", taskId);
            String content;
            switch (errorCode) {
                case "1":
                    content = "巡视设备异常";
                    break;
                case "2":
                    content = "无权限（或高优先级任务存在）";
                    break;
                default:
                    content = "其它异常";
                    break;
            }
            params.put("content", content);
            Constant.restTemplateGet(Constant.TASK_SHUT_DOWN_URL, params);
        }
    }
}
