package com.yjh.platform.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.netflix.discovery.converters.Auto;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.SysUserDevicePermissionDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.SysUserDevicePermissionDO;
import com.yjh.platform.module.user.entity.enums.UserStateEnum;
import com.yjh.platform.module.user.entity.input.DevicePermissionCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Service
public class DevicePermissionService {
    @Autowired
    private SysUserDevicePermissionDao sysUserDevicePermissionDao;

    @Autowired
    private SysUserDao sysUserDao;

    public int insert(DevicePermissionCommand devicePermissionCommand) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(devicePermissionCommand.getUserId());
        if (Objects.nonNull(sysUser) && UserStateEnum.INVALID.getCode() == sysUser.getState()) {
            throw new BusinessException("用户不存在或已删除");
        }

        List<SysUserDevicePermissionDO> insertList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(devicePermissionCommand.getDeviceIds())) {
            List<Long> deviceIds = devicePermissionCommand.getDeviceIds();
            deviceIds.forEach(deviceId -> {
                SysUserDevicePermissionDO sysUserDevicePermissionDO = new SysUserDevicePermissionDO();
                sysUserDevicePermissionDO.setUserId(devicePermissionCommand.getUserId());
                sysUserDevicePermissionDO.setMonitorDeviceId(deviceId);
                insertList.add(sysUserDevicePermissionDO);
            });
        }
        sysUserDevicePermissionDao.delete(devicePermissionCommand.getUserId());
        if (CollectionUtils.isNotEmpty(insertList)) {
            return sysUserDevicePermissionDao.batchInsert(insertList);
        }
        return 0;
    }

    public List<SysUserDevicePermissionDO> getSelectedByUserId(Long userId) {
        return sysUserDevicePermissionDao.getSelectedByUserId(userId);
    }
}
