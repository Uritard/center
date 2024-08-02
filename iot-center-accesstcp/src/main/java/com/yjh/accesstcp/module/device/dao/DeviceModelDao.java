package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TStdMete;
import com.yjh.platform.module.task.entity.TStdDevicemete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DeviceModelDao {

    int batchAdd(List<TStdMete> list);

    int batchAddDeviceMetes(List<TStdDevicemete> list);

    List<Map<String, String>> selectDictCodeByUpDict(@Param("colName") String colName);

    void cleanAll();
}
