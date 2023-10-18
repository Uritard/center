/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.utils.ResultConvertUtil;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.*;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.entity.UPatrolTaskAttr;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/10/16
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class UpdatePatrolService {

    @Autowired
    private UPatrolTaskDao uPatrolTaskDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private UPatrolTaskAttrDao uPatrolTaskAttrDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private UPatrolDataResultDao uPatrolDataResultDao;
    @Autowired
    private CruiseUpPatrolDao cruiseUpPatrolDao;

    private static final int PAGE_SIZE = 2000;

    private String areaId;

    private static final Map<String, AtomicLong> UPDATE_STEP = Collections.synchronizedMap(new LinkedHashMap<>(16));

    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, AtomicLong> cruiseUptoPatrol() {
        if (!UPDATE_STEP.isEmpty()) {
            log.info("数据已开始迁移，返回迁移进度");
            return UPDATE_STEP;
        }
        UPDATE_STEP.put("开始迁移数据库表", new AtomicLong(System.currentTimeMillis()));
        areaId = (String)redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content");

        // 获取当前代理，走事物和异步处理
        UpdatePatrolService proxy = SpringBeanUtils.getBean(UpdatePatrolService.class);
        assert proxy != null;
        proxy.startUpdatePatrol();

        return UPDATE_STEP;
    }

    public synchronized String cruiseUpReset() {
        if (!UPDATE_STEP.isEmpty() && UPDATE_STEP.size() < 6) {
            return "数据正在迁移，不可重置迁移状态";
        }
        UPDATE_STEP.clear();
        return "数据迁移状态重置，可重新迁移数据";
    }

    @Transactional(rollbackFor = Exception.class)
    @Async
    public void startUpdatePatrol() {
        long t1 = System.currentTimeMillis();
        updatePatrolTask();
        long t2 = System.currentTimeMillis();
        UPDATE_STEP.put("迁移 t_cruise_task -> u_patrol_task 完成", new AtomicLong(t2 - t1));

        updatePatrolTaskAttr();
        long t3 = System.currentTimeMillis();
        UPDATE_STEP.put("迁移 t_cruise_task_attr -> u_patrol_task_attr 完成", new AtomicLong(t3 - t2));

        updatePatrolResult();
        long t4 = System.currentTimeMillis();
        UPDATE_STEP.put("迁移 t_cruise_result -> u_patrol_result 完成", new AtomicLong(t4 - t3));

        updatePatrolDetail();
        long t5 = System.currentTimeMillis();
        UPDATE_STEP.put("迁移 t_cruise_task_result_detail,t_cruise_data_result -> u_patrol_data_result 完成", new AtomicLong(t5 - t4));
        UPDATE_STEP.put("迁移数据库表完成", new AtomicLong(t5 - t1));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePatrolTask() {
        AtomicLong count = new AtomicLong();
        UPDATE_STEP.put("迁移 t_cruise_task -> u_patrol_task", count);

        int i = 0;
        try {
            while (++i > 0) {
                Page page = PageHelper.startPage(i, PAGE_SIZE, true, null, true);
                List<TCruiseTask> cruiseTaskList = tCruiseTaskDao.selectByPage(new TCruiseTask());

                CopyOptions copyOptions = CopyOptions.create();
                Map<String, String> fieldMapping = new HashMap<>();
                fieldMapping.put("type", "taskType");
                fieldMapping.put("ifRun", "executeType");
                copyOptions.setIgnoreNullValue(true);
                copyOptions.setFieldMapping(fieldMapping);

                List<UPatrolTask> patrolTaskList = cruiseTaskList.stream().map((source) -> {
                    UPatrolTask target = new UPatrolTask();
                    BeanUtil.copyProperties(source, target, copyOptions);
                    target.setAreaId(areaId);
                    return target;
                }).collect(Collectors.toList());

                if (!patrolTaskList.isEmpty()) {
                    uPatrolTaskDao.batchAdd(patrolTaskList);
                    long ins = count.addAndGet(patrolTaskList.size());
                    log.info("【历史数据迁移】 u_patrol_task insert count: {}", ins);
                }

                if (cruiseTaskList.size() < PAGE_SIZE) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("迁移数据错误：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePatrolTaskAttr() {
        AtomicLong count = new AtomicLong();
        UPDATE_STEP.put("迁移 t_cruise_task_attr -> u_patrol_task_attr", count);

        List<UPatrolTaskAttr> taskAttrList = new ArrayList<>();
        // 大量数据使用游标查询
        try (Cursor<UPatrolTaskAttr> taskSource = cruiseUpPatrolDao.selectPatrolCursor()) {
            taskSource.forEach(u -> {
                taskAttrList.add(u);
                if (taskAttrList.size() >= PAGE_SIZE) {
                    uPatrolTaskAttrDao.batchAdd(taskAttrList);
                    long ins = count.addAndGet(taskAttrList.size());
                    log.info("【历史数据迁移】 u_patrol_task_attr insert count: {}", ins);
                    taskAttrList.clear();
                }
            });

            if (!taskAttrList.isEmpty()) {
                uPatrolTaskAttrDao.batchAdd(taskAttrList);
                long ins = count.addAndGet(taskAttrList.size());
                log.info("【历史数据迁移】 u_patrol_task_attr insert count: {}", ins);
            }
        } catch (Exception e) {
            log.error("迁移数据错误：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePatrolResult() {
        AtomicLong count = new AtomicLong();
        UPDATE_STEP.put("迁移 t_cruise_result -> u_patrol_result", count);

        List<UPatrolResult> taskAttrList = new ArrayList<>();
        // 大量数据使用游标查询
        try (Cursor<UPatrolResult> taskSource = cruiseUpPatrolDao.selectResultCursor()) {
            taskSource.forEach(u -> {
                u.setAreaId(areaId);
                if (StringUtils.isNotEmpty(u.getCheckUser())) {
                    u.setRemark("1");
                }
                taskAttrList.add(u);
                if (taskAttrList.size() >= PAGE_SIZE) {
                    uPatrolResultDao.batchAdd(taskAttrList);
                    long ins = count.addAndGet(taskAttrList.size());
                    log.info("【历史数据迁移】 u_patrol_result insert count: {}", ins);
                    taskAttrList.clear();
                }
            });

            if (!taskAttrList.isEmpty()) {
                uPatrolResultDao.batchAdd(taskAttrList);
                long ins = count.addAndGet(taskAttrList.size());
                log.info("【历史数据迁移】 u_patrol_result insert count: {}", ins);
            }
        } catch (Exception e) {
            log.error("迁移数据错误：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePatrolDetail() {
        AtomicLong count = new AtomicLong();
        UPDATE_STEP.put("迁移 t_cruise_task_result_detail,t_cruise_data_result -> u_patrol_data_result", count);

        List<UPatrolDataResult> taskAttrList = new ArrayList<>();
        // 大量数据使用游标查询
        try (Cursor<UPatrolDataResult> taskSource = cruiseUpPatrolDao.selectPatrolDetailCursor()) {
            taskSource.forEach(u -> {
                if (ArrayUtils.contains(new Integer[] {250, 409}, u.getCruiseAbnormal())) {
                    u.setCruiseAbnormal(null);
                    u.setIsWarn(1);
                    u.setCruiseResult(CruiseConstant.CRUISE_RESULT_NORMAL);
                }
                String resultNum = descNumMapping.get(u.getResultDesc());
                if (StringUtils.isEmpty(resultNum)) {
                    resultNum = ResultConvertUtil.convertResult(u.getResultDesc());
                }
                u.setResultNum(resultNum);

                String modifyNum = descNumMapping.get(u.getPersonCheck());
                if (StringUtils.isEmpty(modifyNum)) {
                    modifyNum = ResultConvertUtil.convertResult(u.getPersonCheck());
                }
                u.setModifyNum(modifyNum);

                taskAttrList.add(u);
                if (taskAttrList.size() >= PAGE_SIZE) {
                    uPatrolDataResultDao.batchInsertUPatrolDataResult(taskAttrList);
                    long ins = count.addAndGet(taskAttrList.size());
                    log.info("【历史数据迁移】 u_patrol_data_result insert count: {}", ins);
                    taskAttrList.clear();
                }
            });

            if (!taskAttrList.isEmpty()) {
                uPatrolDataResultDao.batchInsertUPatrolDataResult(taskAttrList);
                long ins = count.addAndGet(taskAttrList.size());
                log.info("【历史数据迁移】 u_patrol_data_result insert count: {}", ins);
            }
        } catch (Exception e) {
            log.error("迁移数据错误：", e);
        }
    }

    private static Map<String, String> descNumMapping = new HashMap<>(128);

    static {
        descNumMapping.put("投", "1");
        descNumMapping.put("退", "1");
        descNumMapping.put("遥控", "1");
        descNumMapping.put("预合", "2");
        descNumMapping.put("灭", "0");
        descNumMapping.put("亮", "1");
        descNumMapping.put("合闸", "2");
        descNumMapping.put("未接地", "2");
        descNumMapping.put("分闸", "1");
        descNumMapping.put("预分", "3");
        descNumMapping.put("UCA", "1");
        descNumMapping.put("自动", "0");
        descNumMapping.put("运行", "1");
        descNumMapping.put("未运行", "0");
        descNumMapping.put("off", "0");
        descNumMapping.put("green", "3");
        descNumMapping.put("解除", "1");
        descNumMapping.put("投入", "0");
        descNumMapping.put("合位", "2");
        descNumMapping.put("UBC", "1");
        descNumMapping.put("主硅链", "1");
        descNumMapping.put("备用硅链", "2");
        descNumMapping.put("接地", "1");
        descNumMapping.put("未告警", "0");
        descNumMapping.put("未跳闸", "0");
        descNumMapping.put("未报警", "0");
        descNumMapping.put("市电接入", "1");
        descNumMapping.put("故障", "1");
        descNumMapping.put("非故障", "0");
        descNumMapping.put("UAB", "2");
        descNumMapping.put("告警", "1");
        descNumMapping.put("发电机接入", "1");
        descNumMapping.put("暗", "1");
        descNumMapping.put("绿", "3");
        descNumMapping.put("跳闸", "1");
        descNumMapping.put("跳位", "2");
        descNumMapping.put("报警", "1");
        descNumMapping.put("无状态", "0");
        descNumMapping.put("备用", "1");
        descNumMapping.put("分位", "1");
        descNumMapping.put("red", "4");
        descNumMapping.put("手动", "1");
        descNumMapping.put("非保护", "1");
        descNumMapping.put("已录像", "1");
        descNumMapping.put("红", "4");
        descNumMapping.put("无电", "1");
        descNumMapping.put("录像成功", "1");
        descNumMapping.put("带电", "1");
    }

}
