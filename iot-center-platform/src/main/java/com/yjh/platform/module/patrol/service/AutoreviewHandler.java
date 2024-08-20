/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.DateUtils;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.module.devicemete.dao.TCfgAutoreviewMapper;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreview;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreviewDetail;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.TDefectInfo;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.service.TAlgorithmInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/24
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoreviewHandler {
    private final TCfgAutoreviewMapper autoreviewMapper;
    private final TAlgorithmInfoService algorithmInfoService;
    private final RedisTemplate redisTemplate;

    public static final String AUTOREVIEW_TYPE_CRUISE = "1";
    public static final String AUTOREVIEW_TYPE_ALARM = "2";

    private volatile Map<String, List<Map<Integer, Set<String>>>> autoreviewMap;

    private List<Map<Integer, Set<String>>> loadAutoreviewList(String autoreviewType) {
        if (MapUtils.isEmpty(autoreviewMap)) {
            synchronized (AutoreviewHandler.class) {
                if (MapUtils.isEmpty(autoreviewMap)) {
                    log.info("加载自动审核配置...");
                    try {
                        List<TCfgAutoreview> detailList = autoreviewMapper.detailList(null, -1, null, null);

                        Map<String, List<Map<Integer, Set<String>>>> innerMap = new HashMap<>();

                        detailList.forEach(de -> {
                            if (CollectionUtils.isNotEmpty(de.getDetail())) {
                                Map<Integer, Set<String>> dMap = de.getDetail()
                                    .stream()
                                    .collect(Collectors.groupingBy(TCfgAutoreviewDetail::getAutoDetailType,
                                        Collectors.mapping(TCfgAutoreviewDetail::getRefId, Collectors.toSet())));
                                String[] types = StringUtils.split(de.getAutoreviewType(), ",");
                                for (String type : types) {
                                    innerMap.computeIfAbsent(type, k -> new ArrayList<>()).add(dMap);
                                }
                            }
                        });

                        autoreviewMap = innerMap;
                        log.info("加载自动审核配置成功...");
                    } catch (Exception e) {
                        log.error("加载自动审核配置失败", e);
                    }
                }
            }
        }
        return autoreviewMap.containsKey(autoreviewType) ? new ArrayList<>(autoreviewMap.get(autoreviewType)) : Collections.emptyList();
    }

    public void resetAutoreviewList() {
        synchronized (AutoreviewHandler.class) {
            if (autoreviewMap != null) {
                autoreviewMap.clear();
            }
            autoreviewMap = null;
        }
        log.info("清除缓存自动审核配置...");
    }

    public void autoreviewCheckResult(List<Map<String, String>> cruiseResultList) {
        // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
        for (Map<String, String> cruiseResult : cruiseResultList) {
            try {
                Map<Integer, String[]> metes = new HashMap<>();
                if (MapUtils.getIntValue(cruiseResult, "cruiseResult") != CruiseConstant.CRUISE_RESULT_NORMAL) {
                    if (Constant.logUpLv3()) {
                        log.info("结果异常，不进行无需审核判断");
                    }
                    continue;
                }
                String resultDesc = cruiseResult.getOrDefault("resultDesc", "");
                List<String> defectType = new ArrayList<>();
                for (String defect : StringUtils.split(resultDesc)) {
                    TAlgorithmInfo defectInfo = algorithmInfoService.getAlgorithmInfo(defect);
                    if (defectInfo != null && defectInfo.getDefectType() != null) {
                        defectType.add(String.valueOf(defectInfo.getDefectType()));
                    }
                }
                // 缺陷类型
                if (CollectionUtils.isNotEmpty(defectType)) {
                    metes.put(1, defectType.toArray(new String[0]));
                } else {
                    metes.put(1, ArrayUtils.EMPTY_STRING_ARRAY);
                }

                metes.put(3, new String[] {cruiseResult.getOrDefault("deviceType", "")});
                metes.put(4, new String[] {cruiseResult.getOrDefault("deviceId", "")});
                metes.put(5, new String[] {cruiseResult.getOrDefault("deviceMeteId", "")});
                metes.put(6, StringUtils.split(cruiseResult.getOrDefault("labelAttri", ""), ","));

                if (autoreviewCheck(metes, AUTOREVIEW_TYPE_CRUISE)) {
                    String redisKeyName = PATROL_TASK_PREFIX + cruiseResult.get("taskId") + ":" + cruiseResult.get("instanceId");
                    cruiseResult.put("evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_IGNORE));
                    redisTemplate.opsForHash().put(redisKeyName, "evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_IGNORE));
                }
            } catch (Exception e) {
                log.error("巡视结果自动审核异常", e);
            }
        }

    }

    public boolean autoreviewCheckAlarm(TWarnInfo warnInfo) {
        try {
            if (warnInfo == null) {
                return false;
            }
            // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
            Map<Integer, String[]> metes = new HashMap<>();

            // 告警类型
            if (warnInfo.getWarnType() != null && warnInfo.getWarnType() > 0) {
                metes.put(2, new String[] {String.valueOf(warnInfo.getWarnType())});
            } else {
                metes.put(2, ArrayUtils.EMPTY_STRING_ARRAY);
            }

            metes.put(3, new String[] {warnInfo.getDeviceType()});
            metes.put(4, new String[] {String.valueOf(warnInfo.getDeviceId())});
            metes.put(5, new String[] {String.valueOf(warnInfo.getStdMeteId())});
            metes.put(6, StringUtils.split(warnInfo.getLabelAttri(), ","));

            if (autoreviewCheck(metes, AUTOREVIEW_TYPE_ALARM)) {
                warnInfo.setConfMode(CruiseConstant.ConfModeEnum.AUTO_CHECKED.getCode());
                warnInfo.setDealType(286);
                warnInfo.setDealTime(DateUtils.now());
                warnInfo.setDealInfo("自动审核");
                return true;
            }
        } catch (Exception e) {
            log.error("告警自动审核异常", e);
        }
        return false;
    }

    public boolean autoreviewCheckDefect(TDefectInfo tDefectInfo) {
        try {
            if (tDefectInfo == null) {
                return false;
            }
            // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
            Map<Integer, String[]> metes = new HashMap<>();

            // 缺陷类型
            if (!CommonUtils.isEmptyOrNullstr(tDefectInfo.getDefectType()) ) {
                metes.put(1, tDefectInfo.getDefectType().split(","));
            } else {
                metes.put(1, ArrayUtils.EMPTY_STRING_ARRAY);
            }

            metes.put(3, new String[] {tDefectInfo.getDeviceType()});
            metes.put(4, new String[] {String.valueOf(tDefectInfo.getDeviceId())});
            metes.put(5, new String[] {String.valueOf(tDefectInfo.getStdMeteId())});
            metes.put(6, StringUtils.split(tDefectInfo.getLabelAttri(), ","));

            if (autoreviewCheck(metes, AUTOREVIEW_TYPE_ALARM)) {
                tDefectInfo.setConfMode(CruiseConstant.ConfModeEnum.AUTO_CHECKED.getCode());
                tDefectInfo.setDealType(286);
                tDefectInfo.setDealTime(DateUtils.now());
                tDefectInfo.setDealInfo("自动审核");
                return true;
            }
        } catch (Exception e) {
            log.error("缺陷自动审核异常", e);
        }
        return false;
    }

    private boolean autoreviewCheck(Map<Integer, String[]> metes, String autoreviewType) {
        if (Constant.logUpLv3()) {
            log.info("自动审核判断: {} -- {}", JSON.toJSONString(metes), autoreviewType);
        }
        List<Map<Integer, Set<String>>> reviewList = loadAutoreviewList(autoreviewType);
        boolean flag = false;
        for (Map<Integer, Set<String>> review : reviewList) {
            flag = true;
            for (Map.Entry<Integer, Set<String>> entry : review.entrySet()) {
                String[] val = metes.get(entry.getKey());
                boolean contains = val  == null || (ArrayUtils.isNotEmpty(val) && CollectionUtils.containsAny(entry.getValue(), val));
                if (!contains) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                if (Constant.logUpLv3()) {
                    log.info("自动审核命中: {} -- {}", JSON.toJSONString(review), autoreviewType);
                }
                return flag;
            }
        }

        return flag;
    }
}
