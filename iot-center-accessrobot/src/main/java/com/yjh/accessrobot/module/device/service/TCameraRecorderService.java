package com.yjh.accessrobot.module.device.service;

import com.yjh.accessrobot.module.command.dao.TCameraRecorderDao;
import com.yjh.accessrobot.module.command.entity.TCameraRecorder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/8
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TCameraRecorderService {

    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Transactional
    public void saveReportData(List<TCameraRecorder> tCameraRecorderList, String edgeNode) {
        // 查询该节点历史数据
        List<TCameraRecorder> oldTCameraRecorderList = tCameraRecorderDao.selectByEdgeCode(edgeNode);
        // 无历史数据则全部新增
        if (CollectionUtils.isEmpty(oldTCameraRecorderList)) {
            tCameraRecorderList.forEach(tCameraRecorder -> {
                tCameraRecorder.setOriginId(tCameraRecorder.getRecordId());
                tCameraRecorder.setRecordId(null);
                tCameraRecorder.setEdgeCode(edgeNode);
            });
            tCameraRecorderDao.batchInsert(tCameraRecorderList);
            // 否则对比数据
        } else {
            Map<Long, TCameraRecorder> oldTCameraRecorderMap = oldTCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, Function.identity()));
            Map<Long, TCameraRecorder> newTCameraRecorderMap = tCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getRecordId, Function.identity()));
            // 更新的数据
            Collection<Long> updateIdCollection = CollectionUtils.intersection(oldTCameraRecorderMap.keySet(), newTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                tCameraRecorderList.stream().filter(tCameraRecorder -> updateIdCollection.contains(tCameraRecorder.getRecordId())).forEach(tCameraRecorder -> {
                    TCameraRecorder oldTCameraRecorder = oldTCameraRecorderMap.get(tCameraRecorder.getRecordId());
                    tCameraRecorder.setOriginId(tCameraRecorder.getRecordId());
                    tCameraRecorder.setRecordId(oldTCameraRecorder.getRecordId());
                    tCameraRecorder.setEdgeCode(edgeNode);
                    tCameraRecorderDao.updateByPrimaryKey(tCameraRecorder);
                });
            }
            //删除的数据
            Collection<Long> deleteIdCollection = CollectionUtils.subtract(oldTCameraRecorderMap.keySet(), newTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdCollection)) {
                tCameraRecorderDao.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdCollection);
            }
            //新增的数据
            Collection<Long> insertIdCollection = CollectionUtils.subtract(newTCameraRecorderMap.keySet(), oldTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdCollection)) {
                List<TCameraRecorder> insertTCameraRecorderList = tCameraRecorderList.stream().filter(tCameraRecorder -> insertIdCollection.contains(tCameraRecorder.getRecordId())).peek(tCameraRecorder -> {
                    tCameraRecorder.setOriginId(tCameraRecorder.getRecordId());
                    tCameraRecorder.setRecordId(null);
                    tCameraRecorder.setEdgeCode(edgeNode);
                }).collect(Collectors.toList());
                tCameraRecorderDao.batchInsert(insertTCameraRecorderList);
            }
        }
    }
}
