package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.SysLogs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tt
 * @since 2020-08-12
 */
@Repository
public interface PatrolTaskDao {

    List<Map<String,Object>> selectTaskInfo();

    List<Long> selectInstanceId(@Param(value = "taskId") String taskId);

    HashMap<String,Object> countInstanceLoss(@Param(value = "startTime")String startTime,
                                             @Param(value = "endTime")String endTime );

    HashMap<String,Object> countResultCheck(@Param(value = "startTime")String startTime,
                                            @Param(value = "endTime")String endTime );


}
