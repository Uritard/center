package com.yjh.platform.module.patrol.service;

import com.google.common.collect.Lists;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.REISSUE_PREFIX;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/8/5
 * @since [产品/模块版本] （可选）
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ReissueResultHandler {

    private final RedisTemplate redisTemplate;

    private final UPatrolDataResultService uPatrolDataResultService;

    private final UPatrolResultDao uPatrolResultDao;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateReissueResult() {
        Set<String> member = redisTemplate.opsForSet().members(REISSUE_PREFIX);
        log.info("获取补发点位信息==》 {}", member);
        if (CollectionUtils.isNotEmpty(member)) {
            Object[] delete = member.toArray(new String[0]);
            Long deleteNum = redisTemplate.opsForSet().remove(REISSUE_PREFIX, delete);
            log.info("删除已处理结果数==》 {}", deleteNum);
            Set<String> taskInfos = new HashSet<>();
            List<UPatrolDataResult> uPatrolDataResultList = new ArrayList<>();
            member.forEach(key -> {
                Map<String, String> resultMap = redisTemplate.opsForHash().entries(key);
                String taskId = MapUtils.getString(resultMap, "taskId", "");
                taskInfos.add(taskId);
                UPatrolDataResult uPatrolDataResult = new UPatrolDataResult();
                uPatrolDataResult.setTaskId(taskId);
                uPatrolDataResult.setInstanceId(NumberUtils.toLong(MapUtils.getString(resultMap, "instanceId", "")));
                uPatrolDataResult.setResultNum(MapUtils.getString(resultMap, "resultNum", ""));
                uPatrolDataResult.setResultDesc(MapUtils.getString(resultMap, "resultDesc", ""));
                uPatrolDataResult.setPicpath(MapUtils.getString(resultMap, "picpath", ""));
                uPatrolDataResult.setCruiseStatus(NumberUtils.toInt(resultMap.get("cruiseStatus")));
                uPatrolDataResult.setOrigpic(MapUtils.getString(resultMap, "origpic", ""));
                uPatrolDataResult.setCruiseResult(NumberUtils.toInt(resultMap.get("cruiseResult")));
                uPatrolDataResult.setCruiseAbnormal(NumberUtils.toInt(resultMap.get("cruiseAbnormal")));
                uPatrolDataResult.setUnit(MapUtils.getString(resultMap, "unit", ""));
                uPatrolDataResultList.add(uPatrolDataResult);
            });
            int maxLength = 1000;
            if (uPatrolDataResultList.size() > maxLength) {
                List<List<UPatrolDataResult>> lists = Lists.partition(uPatrolDataResultList, 1000);
                lists.forEach(uPatrolDataResultService::batchUpdateByTaskIdAndInstanceId);
            } else {
                uPatrolDataResultService.batchUpdateByTaskIdAndInstanceId(uPatrolDataResultList);
            }
            taskInfos.forEach(taskId -> {
                UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
                //未执行点位/ 异常点位 直接查库
                Map<String, Object> resMap = uPatrolDataResultService.selectWaitAndAbnormalByTaskId(taskId);
                int taskWait = MapUtils.getInteger(resMap, "taskWait");
                int abnormal = MapUtils.getInteger(resMap, "abnormal");
                uPatrolResult.setTaskAbnormal(abnormal).setTaskWait(taskWait);
                log.info("更新任务 {}信息==》 {}", taskId, uPatrolResult);
                uPatrolResultDao.update(uPatrolResult);
                uPatrolDataResultService.updateCruiseAnalyze(taskId);
            });
        }
    }
}
