package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TStdMete;
import com.yjh.platform.module.task.entity.TStdDevicemete;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceModelDao {
    int batchAdd(List<TStdMete> list);

    int batchAddDeviceMetes(List<TStdDevicemete> list);

}
