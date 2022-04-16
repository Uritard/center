package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TStdDeviceTypeModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hyh
 * @since 2022/4/15
 **/
@Repository
public interface TStdDeviceTypeModelDao {

    /**
     * 新增设备类型模型
     * @param tStdDeviceTypeModel
     * @return
     */
    int add(TStdDeviceTypeModel tStdDeviceTypeModel);

    /**
     * 删除设备类型模型
     * @param deviceTypeId
     * @return
     */
    int deleteByPrimaryId(Long deviceTypeId);

    /**
     * 更新设备类型模型
     * @param tStdDeviceTypeModel
     * @return
     */
    int update(TStdDeviceTypeModel tStdDeviceTypeModel);

    /**
     * 查询设备类型模型
     * @param deviceTypeId
     * @return
     */
    TStdDeviceTypeModel selectByPrimaryId(Long deviceTypeId);
}
