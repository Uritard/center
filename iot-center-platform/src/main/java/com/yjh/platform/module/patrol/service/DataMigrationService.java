package com.yjh.platform.module.patrol.service;

import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.entity.UPatrolTaskAttr;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2023/09/19
 */
@Slf4j
@Service
public class DataMigrationService {

    @Autowired
    private UPatrolTaskDao uPatrolTaskDao;
    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private UPatrolTaskAttrDao uPatrolTaskAttrDao;

    /**
     * t_cruise_plan_attr -> u_patrol_plan_attr
     *
     * @return
     */
    @Transactional
    public int migrationCruisePlanAttr() {
        log.info("表t_cruise_plan_attr 开始迁移！ ");
        List<UPatrolPlanAttr> tCruisePlanAttrList = uPatrolTaskDao.selectAllCruisePlanAttr();
        if (!tCruisePlanAttrList.isEmpty()){
            uPatrolTaskDao.deletePlanAttrInfo();

            int batchSize = 2000;
            int dataSize = tCruisePlanAttrList.size();
            int numBatches = (int) Math.ceil((double) dataSize / batchSize);
            for (int i = 0; i < numBatches; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, dataSize);
                List<UPatrolPlanAttr> batch = tCruisePlanAttrList.subList(fromIndex, toIndex);
                uPatrolPlanAttrDao.batchAdd(batch);
            }

        }
        log.info("表t_cruise_plan_attr 迁移完毕！ ");
        return 1;
    }

    /**
     * t_cruise_task -> u_patrol_task
     *
     * @return
     */
    @Transactional
    public int migrationCruiseTask() {
        log.info("表t_cruise_task 开始迁移！ ");
        List<UPatrolTask> uPatrolTaskList = uPatrolTaskDao.selectAllCruiseTask();
        if (!uPatrolTaskList.isEmpty()){
            uPatrolTaskDao.deleteTaskInfo();

            int batchSize = 2000;
            int dataSize = uPatrolTaskList.size();
            int numBatches = (int) Math.ceil((double) dataSize / batchSize);
            for (int i = 0; i < numBatches; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, dataSize);
                List<UPatrolTask> batch = uPatrolTaskList.subList(fromIndex, toIndex);
                uPatrolTaskDao.batchAdd(batch);
            }
        }
        log.info("表t_cruise_task 迁移完毕！ ");
        return 1;
    }

    /**
     * t_cruise_task_attr -> u_patrol_task_attr
     *
     * @return
     */
    @Transactional
    public int migrationCruiseTaskAttr() {
        log.info("t_cruise_task_attr 开始迁移！ ");
        List<UPatrolTaskAttr> uPatrolTaskAttrList = uPatrolTaskDao.selectAllTaskAttr();
        if (!uPatrolTaskAttrList.isEmpty()){
            uPatrolTaskDao.deleteTaskAttrInfo();

            int batchSize = 2000;
            int dataSize = uPatrolTaskAttrList.size();
            int numBatches = (int) Math.ceil((double) dataSize / batchSize);
            for (int i = 0; i < numBatches; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, dataSize);
                List<UPatrolTaskAttr> batch = uPatrolTaskAttrList.subList(fromIndex, toIndex);
                uPatrolTaskAttrDao.batchAdd(batch);
            }
        }
        log.info("t_cruise_task_attr 迁移完毕！ ");
        return 1;
    }

    /**
     * t_cruise_result -> u_patrol_result
     *
     * @return
     */
    @Transactional
    public int migrationCruiseResult() {
        log.info("t_cruise_result 开始迁移！ ");
        List<UPatrolResult> uPatrolResultList = uPatrolTaskDao.selectAllCruiseResult();
        if (!uPatrolResultList.isEmpty()){
            uPatrolTaskDao.deleteResultInfo();

            int batchSize = 2000;
            int dataSize = uPatrolResultList.size();
            int numBatches = (int) Math.ceil((double) dataSize / batchSize);
            for (int i = 0; i < numBatches; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, dataSize);
                List<UPatrolResult> batch = uPatrolResultList.subList(fromIndex, toIndex);
                uPatrolResultDao.batchAdd(batch);
            }
        }
        log.info("t_cruise_result 迁移完毕！ ");
        return 1;
    }

    public void taskDataMigration(){
        log.info("任务相关数据开始迁移！");
        migrationCruisePlanAttr();
        migrationCruiseTask();
        migrationCruiseTaskAttr();
        migrationCruiseResult();
        log.info("任务相关数据迁移完毕！");
    }
}
