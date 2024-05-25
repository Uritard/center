/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.yjh.commons.DateUtils;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


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

    public static final String AUTOREVIEW_TYPE_CRUISE = "1";
    public static final String AUTOREVIEW_TYPE_ALARM = "2";

    private final Map<String, List<Map<Integer, Set<String>>>> autoreviewMap = new ConcurrentHashMap<>(4);

    private List<Map<Integer, Set<String>>> loadAutoreviewList(String autoreviewType) {
        if (MapUtils.isEmpty(autoreviewMap)) {
            synchronized (AutoreviewHandler.class) {
                if (MapUtils.isEmpty(autoreviewMap)) {
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

                    autoreviewMap.putAll(innerMap);
                }
            }
        }
        return new ArrayList<>(autoreviewMap.get(autoreviewType));
    }

    public void resetAutoreviewList() {
        autoreviewMap.clear();
    }

    public void autoreviewCheckResult(List<Map<String, String>> cruiseResultList) {
        // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
        for (Map<String, String> cruiseResult:cruiseResultList) {
            Map<Integer, String[]> metes = new HashMap<>();
            if (MapUtils.getIntValue(cruiseResult,"cruiseResult") != CruiseConstant.CRUISE_RESULT_NORMAL) {
                continue;
            }
            String resultDesc = cruiseResult.getOrDefault("resultDesc", "");
            List<String> defectType = new ArrayList<>();
            for (String defect:StringUtils.split(resultDesc)) {
                TAlgorithmInfo defectInfo = algorithmInfoService.getAlgorithmInfo(defect);
                if (defectInfo != null && defectInfo.getDefectType() != null) {
                    defectType.add(String.valueOf(defectInfo.getDefectType()));
                }
            }
            // 缺陷类型
            if (CollectionUtils.isNotEmpty(defectType)) {
                metes.put(1, defectType.toArray(defectType.toArray(new String[0])));
            }

            metes.put(3, new String[]{cruiseResult.getOrDefault("deviceType", "")});
            metes.put(4, new String[]{cruiseResult.getOrDefault("deviceId", "")});
            metes.put(5, new String[]{cruiseResult.getOrDefault("deviceMeteId", "")});
            metes.put(6, StringUtils.split(cruiseResult.getOrDefault("labelAttri", ""), ","));

            if (autoreviewCheck(metes, AUTOREVIEW_TYPE_CRUISE)) {
                cruiseResult.put("evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_IGNORE));
            }
        }

    }

    public boolean autoreviewCheckAlarm(TWarnInfo warnInfo) {
        if (warnInfo == null) {
            return false;
        }
        // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
        Map<Integer, String[]> metes = new HashMap<>();

        // 告警类型
        if (warnInfo.getAlarmType() != null && warnInfo.getAlarmType() > 0) {
            metes.put(2, new String[]{String.valueOf(warnInfo.getAlarmType())});
        }

        metes.put(3, new String[]{warnInfo.getDeviceType()});
        metes.put(4, new String[]{String.valueOf(warnInfo.getDeviceId())});
        metes.put(5, new String[]{String.valueOf(warnInfo.getStdMeteId())});
        metes.put(6, StringUtils.split(warnInfo.getLabelAttri(), ","));

        if (autoreviewCheck(metes, AUTOREVIEW_TYPE_ALARM)) {
            warnInfo.setConfMode(CruiseConstant.ConfModeEnum.AUTO_CHECKED.getCode());
            warnInfo.setDealType(286);
            warnInfo.setDealTime(DateUtils.now());
            warnInfo.setDealInfo("自动审核");
            return true;
        }
        return false;
    }

    public boolean autoreviewCheckDefect(TDefectInfo tDefectInfo) {
        if (tDefectInfo == null) {
            return false;
        }
        // 1-缺陷类型 2-告警类型 3-设备类型 4-设备 5-测点 6-标签
        Map<Integer, String[]> metes = new HashMap<>();

        // 缺陷类型
        if (tDefectInfo.getDefectType() != null && tDefectInfo.getDefectType() > 0) {
            metes.put(1, new String[]{String.valueOf(tDefectInfo.getDefectType())});
        }

        metes.put(3, new String[]{tDefectInfo.getDeviceType()});
        metes.put(4, new String[]{String.valueOf(tDefectInfo.getDeviceId())});
        metes.put(5, new String[]{String.valueOf(tDefectInfo.getStdMeteId())});
        metes.put(6, StringUtils.split(tDefectInfo.getLabelAttri(), ","));

        if (autoreviewCheck(metes, AUTOREVIEW_TYPE_ALARM)) {
            tDefectInfo.setConfMode(CruiseConstant.ConfModeEnum.AUTO_CHECKED.getCode());
            tDefectInfo.setDealType(286);
            tDefectInfo.setDealTime(DateUtils.now());
            tDefectInfo.setDealInfo("自动审核");
            return true;
        }
        return false;
    }

    private boolean autoreviewCheck(Map<Integer, String[]> metes, String autoreviewType) {
        List<Map<Integer, Set<String>>> reviewList = loadAutoreviewList(autoreviewType);
        boolean flag = false;
        for (Map<Integer, Set<String>> review : reviewList) {
            flag = true;
            for (Map.Entry<Integer, Set<String>> entry : review.entrySet()) {
                String[] val = metes.get(entry.getKey());
                boolean contains = ArrayUtils.isNotEmpty(val) && CollectionUtils.containsAny(entry.getValue(), val);
                if (!contains) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return flag;
            }
        }

        return flag;
    }
}
