package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.entity.TDeviceMaintenance;
import com.yjh.accessrobot.module.command.entity.TDeviceMaintenanceModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/1/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TDeviceMaintenanceService {


    @Transactional(rollbackFor = Exception.class)
    public void saveReportData(List<TDeviceMaintenanceModel> voiceDeviceModelList, String edgeNode) {
        if(CollectionUtils.isEmpty(voiceDeviceModelList)){
            log.info("robotModelList is null");
            return ;
        }

    }
}
