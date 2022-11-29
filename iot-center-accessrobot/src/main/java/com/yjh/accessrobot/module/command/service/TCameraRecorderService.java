package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.dao.TCameraRecorderDao;
import com.yjh.accessrobot.module.command.entity.TCameraRecorder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    @Resource
    private ServiceRestTemplate serviceRestTemplate;

    @Transactional
    public void saveReportData(List<TCameraRecorder> tCameraRecorderList, String edgeNode) {
        if (CollectionUtils.isEmpty(tCameraRecorderList)) {
            log.info("tCameraRecorderList is null");
            tCameraRecorderDao.deleteByEdgeCode(edgeNode);
            return;
        }
        tCameraRecorderList.forEach(tCameraRecorder -> {
            tCameraRecorder.setOriginId(tCameraRecorder.getRecordId().toString());
            tCameraRecorder.setRecordId(null);
            tCameraRecorder.setEdgeCode(edgeNode);
        });
        // 查询该节点历史数据
        List<TCameraRecorder> oldTCameraRecorderList = tCameraRecorderDao.selectByEdgeCode(edgeNode);
        // 无历史数据则全部新增
        if (CollectionUtils.isEmpty(oldTCameraRecorderList)) {
            tCameraRecorderDao.batchInsert(tCameraRecorderList);
            tCameraRecorderList.forEach(tCameraRecorder -> registerNVR(tCameraRecorder.getRecordId()));
            // 否则对比数据
        } else {
            Map<String, TCameraRecorder> oldTCameraRecorderMap = oldTCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, Function.identity()));
            Map<String, TCameraRecorder> newTCameraRecorderMap = tCameraRecorderList.stream().collect(Collectors.toMap(TCameraRecorder::getOriginId, Function.identity()));
            // 更新的数据
            SetUtils.SetView<String> updateIdCollection = SetUtils.intersection(oldTCameraRecorderMap.keySet(), newTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                tCameraRecorderList.stream().filter(tCameraRecorder -> updateIdCollection.contains(tCameraRecorder.getOriginId())).forEach(tCameraRecorder -> {
                    TCameraRecorder oldTCameraRecorder = oldTCameraRecorderMap.get(tCameraRecorder.getOriginId());
                    tCameraRecorder.setRecordId(oldTCameraRecorder.getRecordId());
                    tCameraRecorderDao.updateByPrimaryKey(tCameraRecorder);
                    registerNVR(tCameraRecorder.getRecordId());
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdCollection = SetUtils.difference(oldTCameraRecorderMap.keySet(), newTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdCollection)) {
                tCameraRecorderDao.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdCollection);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdCollection = SetUtils.difference(newTCameraRecorderMap.keySet(), oldTCameraRecorderMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdCollection)) {
                List<TCameraRecorder> insertTCameraRecorderList = tCameraRecorderList.stream().filter(tCameraRecorder -> insertIdCollection.contains(tCameraRecorder.getOriginId())).collect(Collectors.toList());
                tCameraRecorderDao.batchInsert(insertTCameraRecorderList);
                insertTCameraRecorderList.forEach(tCameraRecorder -> registerNVR(tCameraRecorder.getRecordId()));
            }
        }
    }

    private void registerNVR(Long recordId) {
        try {
            Result result = serviceRestTemplate.getForObject(Constant.NVR_REGISTER_URL, Result.class, recordId);
            log.info("recordId:{} ,result:{}", recordId,result);
        } catch (Exception e) {
            log.error("录像机注册失败 recordId:{}",recordId,e);
        }
    }
}
