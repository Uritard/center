package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;
import com.yjh.platform.module.task.dao.TUnionTaskDao;
import com.yjh.platform.module.task.entity.TUnionTask;
import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Service
public class TUnionTaskAttrService {

    @Autowired
    private TUnionTaskAttrDao tUnionTaskAttrDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTaskAttr tUnionTaskAttr) {
        return this.tUnionTaskAttrDao.insert(tUnionTaskAttr);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String deleteByPrimaryId(Map<String, Object> map) {
        return this.tUnionTaskAttrDao.deleteByPrimaryId(map);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTaskAttr tUnionTaskAttr){
        return this.tUnionTaskAttrDao.update(tUnionTaskAttr);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTaskAttr selectByPrimaryId(String UnionId){
        return this.tUnionTaskAttrDao.selectByPrimaryId(UnionId);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> select(String TaskId,Long instanceId,Long DeviceMeteId,String DeviceCustomId,
                                       String PointTaskId,Integer IfRobot,Integer IfVideo,Integer IfInferad,Integer IfArtificial){
        List<TUnionTaskAttr> list = this.tUnionTaskAttrDao.select(TaskId,instanceId,DeviceMeteId,DeviceCustomId,
                PointTaskId,IfRobot,IfVideo,IfInferad,IfArtificial);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskAttr> selectByPage(TUnionTaskAttr tUnionTaskAttr){
        return this.tUnionTaskAttrDao.select(tUnionTaskAttr);
    }
}

