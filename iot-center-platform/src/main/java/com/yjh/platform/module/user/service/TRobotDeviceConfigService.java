package com.yjh.platform.module.user.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.TreesUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.TRobotDeviceConfigDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotDeviceConfig;
import com.yjh.platform.module.user.entity.TRobotMapNodeDeviceInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/7/5
 * @since [产品/模块版本] （可选）
 */
@Service
public class TRobotDeviceConfigService {

    @Resource
    private TRobotDeviceConfigDao tRobotDeviceConfigDao;

    @Resource
    private TRobotInfoDao tRobotInfoDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotDeviceConfig tRobotDeviceConfig, String userName) {
        List<TRobotDeviceConfig> list = tRobotDeviceConfigDao.selectDeviceConfigByRobotId(tRobotDeviceConfig.getRobotId());
        boolean b = list.stream().anyMatch(m -> m.getEquipmentId().equals(tRobotDeviceConfig.getEquipmentId()));
        if (b){
            throw new BusinessException("该设备已经绑定，请重新选择！");
        }
        tRobotDeviceConfig.setUpdatePerson(userName);
        tRobotDeviceConfig.setUpdateTime(new Date());
        return tRobotDeviceConfigDao.insert(tRobotDeviceConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long deviceConfigId) {
        return tRobotDeviceConfigDao.deleteByPrimaryKey(deviceConfigId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Object deleteByRobotId(Long robotId) {
        return tRobotDeviceConfigDao.deleteByRobotId(robotId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByPrimaryKey(TRobotDeviceConfig tRobotDeviceConfig, String userName) {
        tRobotDeviceConfig.setUpdatePerson(userName);
        tRobotDeviceConfig.setUpdateTime(new Date());
        return tRobotDeviceConfigDao.updateByPrimaryKey(tRobotDeviceConfig);
    }

    public TRobotDeviceConfig selectByPrimaryKey(Long deviceConfigId) {
        return tRobotDeviceConfigDao.selectByPrimaryKey(deviceConfigId);
    }

    public Map<String, Object> selectDeviceConfigByRobotId(Long robotId) {
        Map<String, Object> map = new HashMap<>(2);
        Integer entrance = tRobotInfoDao.selectEntrance(robotId);
        map.put("entrance", entrance);
        List<TRobotDeviceConfig> list = tRobotDeviceConfigDao.selectDeviceConfigByRobotId(robotId);
        map.put("list", list);
        return map;
    }

    /**
     * 查询设备与地图点绑定信息（去看看功能使用）
     * @param robotId
     * @return
     */
    public Map<String, Object> selectMapNodeDeviceByRobotId(Long robotId) {
        Map<String, Object> map = new HashMap<>(2);
        Integer entrance = tRobotInfoDao.selectEntrance(robotId);
        map.put("entrance", entrance);
        List<TRobotMapNodeDeviceInfo> list =  tRobotDeviceConfigDao.selectMapNodeDeviceByRobotId(robotId);
        map.put("list", list);
        return map;
    }

    public int updateEntrance(Long robotId, Integer entrance) {
        return tRobotInfoDao.updateEntrance(robotId, entrance);
    }

    public List<AreaInfo> selectDeviceTree() {
        List<AreaInfo> list = tRobotDeviceConfigDao.selectDeviceTree();
        return TreesUtil.assembleTrees(list);
    }


}
