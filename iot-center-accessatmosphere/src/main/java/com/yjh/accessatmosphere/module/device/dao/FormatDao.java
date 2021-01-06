package com.yjh.accessatmosphere.module.device.dao;

import java.util.List;

import com.yjh.accessatmosphere.module.device.entity.Format;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-20
 */
@Repository
public interface FormatDao {

    int insert(Format format);
    int deleteByPrimaryId(@Param(value = "testId") String testId);
    int update(Format format);
    Format selectByPrimaryId(@Param(value = "testId") String testId);
    List<Format> select(@Param(value = "testId") String testId,
                                @Param(value = "testType") String testType);
    List<Format> selectByPage(Format format);

    int batchInsert(List<Format> list);
}
