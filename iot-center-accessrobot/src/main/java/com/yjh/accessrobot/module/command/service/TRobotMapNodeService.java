package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TRobotMapNodeDao;
import com.yjh.accessrobot.module.command.entity.TRobotMapNode;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @author quzhihui
 * @date 2022/10/8 - 17:56
 */
@Service
public class TRobotMapNodeService {

    @Autowired
    private TRobotMapNodeDao tRobotMapNodeDao;

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotMapNode> list) {
        return this.tRobotMapNodeDao.batchInsertTMapNodes(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByRobotId(Long robotId) {
        this.tRobotMapNodeDao.deleteByRobotId(robotId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotMapNode> selectByRobotId(Long robotId){
        return tRobotMapNodeDao.selectByRobotId(robotId);
    }

    public int updateByPrimaryKey(TRobotMapNode tRobotMapNode) {
        return tRobotMapNodeDao.updateByPrimaryKey(tRobotMapNode);
    }

    public int deleteByRobotIdAndNodeId(Long robotId, Collection<String> deleteIdSet) {
        return tRobotMapNodeDao.deleteByRobotIdAndNodeId(robotId, deleteIdSet);
    }

    public int deleteRobotDeviceConfig(Long robotId, Collection<String> deleteIdSet) {
        return tRobotMapNodeDao.deleteRobotDeviceConfig(robotId, deleteIdSet);
    }

    public int updateTStdDevice(Long robotId, Collection<String> deleteIdSet) {
        return tRobotMapNodeDao.updateTStdDevice(robotId, deleteIdSet);
    }
}
