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

    private final String taskPatrolledId;
    private final String errorCode;
    private final RobotService robotService;

    public TaskShutDownThread(RobotService robotService, String taskPatrolledId, String errorCode){
        this.robotService = robotService;
        this.taskPatrolledId = taskPatrolledId;
        this.errorCode = errorCode;
    }

    @Override
    public void run() {
        // 增加时间判断，避免预先初始化导致数据传入下一个任务
        String taskCode = StringUtils.substringBetween(taskPatrolledId, "_");
        String[] patrolledIds = taskPatrolledId.split("_");
        String timeStr = patrolledIds.length > 2 ? patrolledIds[2] : patrolledIds[1];
        Date date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
        String taskId = robotService.selectRealTaskId(taskCode, date);
        if (Optional.ofNullable(taskId).isPresent()){
            Map<String, Object> params = new HashMap<>(1);
            params.put("taskId", taskId);
            Constant.restTemplateGet(Constant.TASK_SHUT_DOWN_URL, params);
        }
    }
}
