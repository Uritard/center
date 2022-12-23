package com.yjh.accesstcp.module.device.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhou Pengcheng
 * @since 2022-04-13
 */
@Repository
public interface StatisticsDao {



    List<Map<String, Object>> countWarnCheckByMonth(
            @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,@Param("timeFormat")String time,@Param("timeFlag")String flag);

    List<Map<String, Object>> countWarnAccuracyByMonth(
            @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,@Param("timeFormat")String time,@Param("timeFlag")String flag);


    List<Map<String, Object>> countInstanceLossByMonth(
            @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,@Param("timeFormat")String time,@Param("timeFlag")String flag);


    List<Map<String, Object>> countResultCheckByDay(
            @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,@Param("timeFormat")String time,@Param("timeFlag")String flag);


    List<Map<String, Object>> countTask(
            @Param(value = "startTime") String startTime, @Param(value = "endTime") String endTime,@Param("timeFormat")String time,@Param("timeFlag")String flag);

}
