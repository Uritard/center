package com.yjh.platform.module.task.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.task.entity.TPeriodModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-09-16
 */
@Repository
public interface TPeriodModelDao {

    int insert(TPeriodModel tPeriodModel);
    int deleteByPrimaryId(@Param(value = "periodId") Long periodId);
    int update(TPeriodModel tPeriodModel);
    TPeriodModel selectByPrimaryId(@Param(value = "periodId") Long periodId);
    List<TPeriodModel> select(@Param(value = "periodId") Long periodId,
                              @Param(value = "cronExpression") String cronExpression,
                              @Param(value = "remark") String remark,
                              @Param(value = "updateTime") Date updateTime,
                              @Param(value = "createTime") Date createTime);
    List<TPeriodModel> selectByPage(TPeriodModel tPeriodModel);

    int batchInsert(List<TPeriodModel> list);
}
