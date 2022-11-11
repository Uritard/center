package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
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

    @Transactional
    public void saveReportData(List<TStdRegion> tStdRegionList, String edgeNode) {
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
        rootTStdRegion.setStationId(reportRootTStdRegion.getStationId());
        rootTStdRegion.setStationName(reportRootTStdRegion.getStationName());
        rootTStdRegion.setRegionPath(reportRootTStdRegion.getRegionPath());
        rootTStdRegion.setRemark(reportRootTStdRegion.getRemark());
        tStdRegionDao.updateByPrimaryKey(rootTStdRegion);
        //保存上报数据非顶层节点数据
        tStdRegionList.removeIf(stdRegion -> stdRegion.getUpRegionId() == -1);

        List<TStdRegion> saveList = new ArrayList<>();
        //查询该下级系统已有数据
        List<TStdRegion> oldStdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, Constant.STATE_SUB);
        if (CollectionUtils.isEmpty(oldStdRegionList)) {
            tStdRegionList.forEach(tStdRegion -> {
                TStdRegion stdRegionNew = convertBean(edgeNode, tStdRegion);
                saveList.add(stdRegionNew);
                tStdRegionDao.insert(stdRegionNew);
            });
        } else {
            Map<String, TStdRegion> oldStdRegionMap = oldStdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, Function.identity()));
            Map<String, TStdRegion> newStdRegionMap = tStdRegionList.stream().collect(Collectors.toMap(tStdRegion -> tStdRegion.getRegionId().toString(), Function.identity()));
            //更新的数据
            SetUtils.SetView<String> updateIdCollection = SetUtils.intersection(oldStdRegionMap.keySet(), newStdRegionMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                newStdRegionMap.entrySet().stream().filter(tStdRegionEntry -> updateIdCollection.contains(tStdRegionEntry.getKey())).forEach(stdRegionEntry -> {
                    TStdRegion tStdRegion = stdRegionEntry.getValue();
                    TStdRegion oldTStdRegion = oldStdRegionMap.get(tStdRegion.getRegionId().toString());
                    tStdRegion.setOriginRegionId(tStdRegion.getRegionId().toString());
                    tStdRegion.setRegionId(oldTStdRegion.getRegionId());
                    tStdRegion.setRegionCode(edgeNode);
                    tStdRegion.setState(Constant.STATE_SUB);
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
                newStdRegionMap.entrySet().stream().filter(stdRegionEntry -> insertIdCollection.contains(stdRegionEntry.getKey())).forEach(stdRegionEntry -> {
                    TStdRegion stdRegionNew = convertBean(edgeNode, stdRegionEntry.getValue());
                    saveList.add(stdRegionNew);
                    tStdRegionDao.insert(stdRegionNew);
                });
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

    public TStdRegion convertBean(String edgeNode, TStdRegion tStdRegion) {
        TStdRegion stdRegionNew = new TStdRegion();
        stdRegionNew.setRegionName(tStdRegion.getRegionName());
        stdRegionNew.setSort(tStdRegion.getSort());
        stdRegionNew.setUpRegionId(tStdRegion.getUpRegionId());
        stdRegionNew.setUpRegionIds(tStdRegion.getUpRegionIds());
        stdRegionNew.setRegionCode(edgeNode);
        stdRegionNew.setOriginRegionId(tStdRegion.getRegionId().toString());
        stdRegionNew.setStationId(tStdRegion.getStationId());
        stdRegionNew.setStationName(tStdRegion.getStationName());
        stdRegionNew.setState(Constant.STATE_SUB);
        stdRegionNew.setEdgeStatus(tStdRegion.getEdgeStatus());
        stdRegionNew.setRegionPath(tStdRegion.getRegionPath());
        stdRegionNew.setRemark(tStdRegion.getRemark());
        stdRegionNew.setCreateTime(tStdRegion.getCreateTime());
        return stdRegionNew;
    }
}
