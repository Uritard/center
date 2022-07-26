package com.yjh.platform.module.device.dao;

import com.yjh.platform.module.device.entity.TStdMeterTypeModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hyh
 * @since 2022/4/15
 **/
@Repository
public interface TStdMeterTypeModelDao {

    /**
     * 新增设备类型模型
     * @param tStdMeterTypeModel
     * @return
     */
    int add(TStdMeterTypeModel tStdMeterTypeModel);

    /**
     * 删除设备类型模型
     * @param deviceTypeId
     * @return
     */
    int deleteByPrimaryId(Long id);

    /**
     * 更新设备类型模型
     * @param tStdMeterTypeModel
     * @return
     */
    int update(TStdMeterTypeModel tStdMeterTypeModel);

    /**
     * 查询设备类型模型
     * @param id
     * @return
     */
    TStdMeterTypeModel selectByPrimaryId(Long id);
}
