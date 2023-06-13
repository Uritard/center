package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/6/13
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class CruiseRetryThread implements Runnable  {

    private UPatrolTaskDao uPatrolTaskDao;
    private UPatrolTaskService uPatrolTaskService;
    private List<Long> missInstanceMapList;
    private String  taskId;

    public CruiseRetryThread(UPatrolTaskDao uPatrolTaskDao, UPatrolTaskService uPatrolTaskService, List<Long> missInstanceMapList,
        String taskId) {
        this.uPatrolTaskDao = uPatrolTaskDao;
        this.uPatrolTaskService = uPatrolTaskService;
        this.missInstanceMapList = missInstanceMapList;
        this.taskId = taskId;
    }

    public CruiseRetryThread() {
    }

    @Override
    public void run() {
        log.info("任务：{}开始重试！",taskId);
        try {

            UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
            UPatrolTask uPatrolTask1 =  uPatrolTaskService.omitInstanceRetry(missInstanceMapList,uPatrolTask);
            if (Objects.nonNull(uPatrolTask1)) {
                // 设置定时器，不走事物逻辑，否则会延时
                uPatrolTaskService.setQuartzTask(uPatrolTask1);
            }
        } catch (Exception e) {
            log.error("遗漏点位任务重试失败",e);
        }
    }
}
