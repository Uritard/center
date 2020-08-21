package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author wf
 * @since 2020-08-19
 */
@Service
public class TCruiseTaskAttrService {

    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskAttr tCruiseTaskAttr) {
        return this.tCruiseTaskAttrDao.insert(tCruiseTaskAttr);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public String deleteByPrimaryId(Map<String, Object> map) {
        return this.tCruiseTaskAttrDao.deleteByPrimaryId(map);
    }

    //修改
    @Logs(title = "修改",code = "moudle")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskAttr tCruiseTaskAttr){
        return this.tCruiseTaskAttrDao.update(tCruiseTaskAttr);
    }

    //主键查询
    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskAttr selectByPrimaryId(String TaskId){
        return this.tCruiseTaskAttrDao.selectByPrimaryId(TaskId);
    }

    //查询
    @Logs(title = "查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskAttr> select(String TaskId,Long InstanceId,Long DeviceMeteId,Long DeviceId,String CustomId,
                                    String PointTaskId,Integer IfRobot,Integer IfVideo,Integer IfInferad,Integer IfArtificial){
        List<TCruiseTaskAttr> list = this.tCruiseTaskAttrDao.select(TaskId,InstanceId,DeviceMeteId,DeviceId,CustomId,
                PointTaskId,IfRobot,IfVideo,IfInferad,IfArtificial);
        return list;
    }

    //分页查询
    @Logs(title = "分页查询",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskAttr> selectByPage(TCruiseTaskAttr tCruiseTaskAttr){
        return this.tCruiseTaskAttrDao.select(tCruiseTaskAttr);
    }
}

