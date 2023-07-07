package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.EdgeEnum;
import com.yjh.accessrobot.module.command.entity.UPatrolTask;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.TaskShutDownThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class ModelFileSyncAndTaskControlHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String countForAbnormalKey = "countForAbnormal:";

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        String sendCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(sendCode, false)) {
            List<Map<String, Object>> resultMapList = xmlBaseModel.getItems();
            if (resultMapList.isEmpty()){
                log.info("机器人收到任务下发指令了,这是机器人的响应");
                return;
            }
            Map<String, Object> resultMap = resultMapList.get(0);
            if (resultMap.size() == 0) {
                log.info("机器人收到检修区域指令了,这是机器人的响应");
                // maintenance area was issued successfully
                robotService.receivingResponse(xmlBaseModel, receiveSessionId);
            } else if(resultMap.containsKey("command")) {
                robotService.dealStatistic(xmlBaseModel.getSendCode(),resultMapList);
            }
            else {
                // 获取 Map中第一个值
                String firstKey = resultMap.entrySet().stream().findFirst().get().getKey();

                if (resultMap.containsKey("task_patrolled_id") || resultMap.containsKey("error_code")) {
                    String errorCode = MapUtils.getString(resultMap, "error_code");
                    String taskMsg = "任务控制指令";
                    if (StringUtils.isNotEmpty(errorCode)) {
                        taskMsg = "联动任务指令";
                        switch (resultMap.get("error_code").toString()) {
                            case "0":
                                log.info("成功");
                                break;
                            case "1":
                                log.info("机器人异常");
                                break;
                            case "2":
                                log.info("无权限（或高优先级任务存在");
                                break;
                            case "3":
                                log.info("其它异常");
                                break;
                            default:
                                break;
                        }
                    }
                    String taskPatrolledId = MapUtils.getString(resultMap, "task_patrolled_id");
                    //所有返回给上级的taskPatrolledId 都是本级别生成的
                    // 变电站编码_taskCode_执行时间
                    log.info("机器人返回的taskPatrolledId：{}",taskPatrolledId);
                    try {
                        String[] taskPatrolledSplit = taskPatrolledId.split("_");
                        String taskCode = "", time = "";
                        if (taskPatrolledSplit.length == 2) {
                            taskCode = taskPatrolledSplit[0];
                            time = taskPatrolledSplit[1];
                        }
                        else {
                            taskCode = taskPatrolledSplit[1];
                            time = taskPatrolledSplit[2];
                        }
                        Date robotTime = DateTimeUtil.parseFormat(time,DateTimeUtil.getDateTimePattern3());
                        //根据taskCode找到taskId
                        UPatrolTask task = robotService.selectTaskIdByTaskCode(taskCode,robotTime);
                        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
                        String taskStartTime = (String) redisTemplate.opsForHash().get(countForAbnormalKey+task.getTaskId(), "taskStart");
                        taskPatrolledId = stationCode+"_"+task.getTaskId()+"_"+DateTimeUtil.format(DateTimeUtil.getDate(taskStartTime), DateTimeUtil.getDateTimePattern3());
                        log.info("本级上报的taskPatrolledId：{}",taskPatrolledId);
                        xmlBaseModel.getItems().get(0).put("task_patrolled_id",taskPatrolledId);
                    }catch (Exception e){
                        log.info("构造本级taskPatrolledId出错：",e);
                    }
                    if (StringUtils.isNotEmpty(errorCode)) {
                        log.info("机器人收到{}了,这是机器人响应的巡视任务执行Id==={}", taskMsg, taskPatrolledId);
                        // 联动的返回结果直接向上反
                        String success = "0";
                        //<0>: =成功
                        //<1>: =巡视设备异常
                        //<2>: = 无权限（或高优先级任务存在）
                        //<3>: = 其它异常
                        //taskPatrolledId 不为空 且 error_code 不为 0 边缘节点任务终止
                        String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
                        AtomicReference<String> taskCode = new AtomicReference<>("");
                        Date date = null;
                        if (StringUtils.isNotEmpty(taskPatrolledId) ) {
                            robotService.upToCruise(xmlBaseModel);
                            taskCode.set(StringUtils.substringBetween(taskPatrolledId, "_"));
                            String timeStr = StringUtils.substringAfterLast(taskPatrolledId, "_");
                            date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
                        }
                        if (StringUtils.isNotEmpty(taskCode.get()) && !success.equals(errorCode) && EdgeEnum.EDGE_NODE.getCode().equals(edgeLevel)){
                            TaskShutDownThread taskShutDownThread = new TaskShutDownThread(robotService, taskCode.get(), errorCode, date);
                            TaskExecutePool.getInstance().execute(taskShutDownThread);
                        }
                    }
                } else if (firstKey.endsWith("_file_path")) {
                    log.info("机器人收到模型指令了,这是机器人的响应");
                    // model file sync
                    for (Map<String, Object> map : resultMapList) {
                        log.info("{} 模型同步的文件路径信息{}", sendCode, map);
                        robotService.addRobotFile(map, sendCode);
                    }
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MODEL_SYNC.getCode(), this);
    }
}
