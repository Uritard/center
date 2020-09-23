package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author tt
 * @since 2020-08-04
 */
@Repository
public interface TDictBusinessDao {

    int insert(TDictBusiness tDictBusiness);
    int deleteByPrimaryId(@Param(value = "dictId") Integer dictId);
    int update(TDictBusiness tDictBusiness);
    TDictBusiness selectByPrimaryId(@Param(value = "dictId") Integer dictId);
    List<TDictBusiness> select(@Param(value = "dictId") Integer dictId,
                                @Param(value = "dictCode") String dictCode,
                                @Param(value = "colName") String colName,
                                @Param(value = "dictNote") String dictNote,
                                @Param(value = "upDict") Integer upDict,
                                @Param(value = "remark") String remark,
                                @Param(value = "sort") Long sort);
    List<TDictBusiness> selectByPage(TDictBusiness tDictBusiness);

    int batchInsert(@Param("list") List<TDictBusiness> list);

    List<TDictBusiness> selectQuery(@Param("colNames") List<String> colNames);
    String selectCameraTypeAndRobotPosition(@Param(value = "colName")String colName,
                                            @Param(value = "dictNote")String dictNote);
    String selectDictNoteByDictCode(@Param(value = "dictCode")String dictCode);

}
