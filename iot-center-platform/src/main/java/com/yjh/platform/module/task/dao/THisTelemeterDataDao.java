package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.THisTelemeterData;
import com.yjh.platform.module.task.entity.UnionTaskInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author czh
 * @since 2020-08-25
 */
@Repository
public interface THisTelemeterDataDao {

    int insert(THisTelemeterData tHisTelemeterData);
    int deleteByPrimaryId(@Param(value = "id")Long id);
    int update(THisTelemeterData tHisTelemeterData);
    THisTelemeterData selectByPrimaryId(@Param(value = "id")Long id);
    List<THisTelemeterData> select(@Param(value = "id")Long id,
                                @Param(value = "meteId") Long meteId,
                                @Param(value = "deviceId") Long deviceId,
                                @Param(value = "recordTime") Date recordTime,
                                @Param(value = "meteKind") Integer meteKind,
                                @Param(value = "meteValue") String meteValue,
                                @Param(value = "lastMeteValue") String lastMeteValue);
    List<THisTelemeterData> selectByPage(THisTelemeterData tHisTelemeterData);

    int batchInsert(List<THisTelemeterData> list);
    List<THisTelemeterData>selectAll(@Param(value = "startTime") Date startTime,
                                     @Param(value = "endTime") Date endTime,
                                     @Param(value = "meteKind") Integer meteKind,
                                     @Param(value = "meteName") String meteName);
    List<UnionTaskInfo> selectUnionTask(@Param(value = "startTime") Date startTime,
                                        @Param(value = "endTime") Date endTime,
                                        @Param(value = "meteKind") Integer meteKind,
                                        @Param(value = "meteName") String meteName);

}
