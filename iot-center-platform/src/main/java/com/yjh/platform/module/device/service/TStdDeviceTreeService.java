package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.DeviceTreeCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备树统一接口查询
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@Service
public class TStdDeviceTreeService {

    private final Logger log = LoggerFactory.getLogger(TStdDeviceTreeService.class);

    public TStdDeviceTreeService() {
    }

    public List<AreaInfo> selectDevTree(DeviceTreeCondition deviceTreeCondition) {
        List<AreaInfo> listTree = new ArrayList<>();
        switch (deviceTreeCondition.getLevel()) {
            case "5":
                break;
            case "6":
                break;
            case "7":
                break;
            case "8":
                break;
            case "9":
                break;
            case "10":
                break;
            default:
                throw new BusinessException("设备树展示层级输入有误！");
        }
        return listTree;
    }

}

