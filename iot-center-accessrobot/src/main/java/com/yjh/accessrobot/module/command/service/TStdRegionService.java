package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/4
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TStdRegionService {

    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Transactional(rollbackFor = Exception.class)
    public void saveReportData(List<TStdRegion> tStdRegionList, String edgeNode, String stationId) {
        // 查询是否创建对应边缘节点
        TStdRegion rootTStdRegion = Optional.of(tStdRegionDao.selectByRegionCodeAndState(edgeNode, Constant.STATE_LOCAL)).map(tStdRegionList1 -> tStdRegionList1.get(0)).orElse(null);
        if (Objects.isNull(rootTStdRegion)) {
            log.error("未配置该边缘节点 edgeNode:{}", edgeNode);
            return;
        }
        //查找上报数据的顶层区域
        TStdRegion reportRootTStdRegion = tStdRegionList.stream().filter(stdRegion -> Long.valueOf(-1).equals(stdRegion.getUpRegionId())).findFirst().orElse(null);
        if (Objects.isNull(reportRootTStdRegion)) {
            log.error("上报数据无顶层节点");
            return;
        }
        // 更新上报数据顶层节点
        rootTStdRegion.setOriginRegionId(reportRootTStdRegion.getRegionId().toString());
        rootTStdRegion.setStationId(stationId);
        rootTStdRegion.setStationName(reportRootTStdRegion.getStationName());
        rootTStdRegion.setRegionPath(reportRootTStdRegion.getRegionPath());
        rootTStdRegion.setRemark(reportRootTStdRegion.getRemark());
        if (StringUtils.isNotEmpty(reportRootTStdRegion.getLatitude())) {
            rootTStdRegion.setLatitude(reportRootTStdRegion.getLatitude());
        }
        if (StringUtils.isNotEmpty(reportRootTStdRegion.getLongitude())) {
            rootTStdRegion.setLongitude(reportRootTStdRegion.getLongitude());
        }
        rootTStdRegion.setVoltageLevel(reportRootTStdRegion.getVoltageLevel());

        tStdRegionDao.updateByPrimaryKey(rootTStdRegion);
        //保存上报数据非顶层节点数据
        tStdRegionList.removeIf(stdRegion -> stdRegion.getUpRegionId() == -1);
        tStdRegionList.forEach(tStdRegion -> {
            tStdRegion.setOriginRegionId(tStdRegion.getRegionId().toString() );
            tStdRegion.setRegionId(null);
            tStdRegion.setState(Constant.STATE_SUB);
            tStdRegion.setRegionCode(edgeNode);
        });
        List<TStdRegion> saveList = new ArrayList<>();
        //查询该下级系统已有数据
        List<TStdRegion> oldStdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, Constant.STATE_SUB);
        // 没有直接新增
        if (CollectionUtils.isEmpty(oldStdRegionList)) {
            tStdRegionList.forEach(tStdRegion -> {
                saveList.add(tStdRegion);
                tStdRegionDao.insert(tStdRegion);
            });
            // 否则比较
        } else {
            Map<String, TStdRegion> oldStdRegionMap = oldStdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, Function.identity()));
            Map<String, TStdRegion> newStdRegionMap = tStdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, Function.identity()));
            //更新的数据
            SetUtils.SetView<String> updateIdCollection = SetUtils.intersection(oldStdRegionMap.keySet(), newStdRegionMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                newStdRegionMap.entrySet().stream().filter(tStdRegionEntry -> updateIdCollection.contains(tStdRegionEntry.getKey())).forEach(stdRegionEntry -> {
                    TStdRegion tStdRegion = stdRegionEntry.getValue();
                    TStdRegion oldTStdRegion = oldStdRegionMap.get(tStdRegion.getOriginRegionId());
                    tStdRegion.setRegionId(oldTStdRegion.getRegionId());
                    saveList.add(tStdRegion);
                    tStdRegionDao.updateByPrimaryKey(tStdRegion);
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdCollection = SetUtils.difference(oldStdRegionMap.keySet(), newStdRegionMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdCollection)) {
                tStdRegionDao.deleteByOriginRegionIdAnRegionCode(Constant.STATE_SUB, edgeNode, deleteIdCollection);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdCollection = SetUtils.difference(newStdRegionMap.keySet(), oldStdRegionMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdCollection)) {
                for (Map.Entry<String, TStdRegion> stdRegionEntry : newStdRegionMap.entrySet()) {
                    if (insertIdCollection.contains(stdRegionEntry.getKey())) {
                        TStdRegion tStdRegion = stdRegionEntry.getValue();
                        saveList.add(tStdRegion);
                        tStdRegionDao.insert(tStdRegion);
                    }
                }
            }
        }
        //新增或更新数据upRegionId转换
        Map<String, Long> map = saveList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, TStdRegion::getRegionId));
        map.put(rootTStdRegion.getOriginRegionId(), rootTStdRegion.getRegionId());
        saveList.forEach(stdRegion -> {
            stdRegion.setUpRegionId(map.get(stdRegion.getUpRegionId().toString()));
            tStdRegionDao.updateUpRegionId(stdRegion);
        });
    }
}
