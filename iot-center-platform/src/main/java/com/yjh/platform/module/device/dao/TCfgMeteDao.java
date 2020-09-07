package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.device.entity.TCfgMete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgMeteDao {

    int insert(TCfgMete tCfgMete);
    int deleteByPrimaryId(@Param(value = "meteId") String meteId);
    int update(TCfgMete tCfgMete);
    TCfgMete selectByPrimaryId(@Param(value = "meteId") String meteId);
    List<TCfgMete> select(@Param(value = "meteId") String meteId,
                                @Param(value = "meteType") String meteType,
                                @Param(value = "meteKind") Integer meteKind,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "unit") String unit,
                                @Param(value = "meteExplainType") String meteExplainType,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime,
                                @Param(value = "modulus") Integer modulus,
                                @Param(value = "stationName") String stationName,
                                @Param(value = "stationId") String stationId,
                                @Param(value = "upEffect") Integer upEffect,
                                @Param(value = "downEffect") Integer downEffect,
                                @Param(value = "alarmlevel") Integer alarmlevel,
                                @Param(value = "alarmthresbhold") Integer alarmthresbhold,
                                @Param(value = "describer") String describer,
                                @Param(value = "metePrecision") Integer metePrecision,
                                @Param(value = "changeLimit") Float changeLimit,
                                @Param(value = "hilimit1") Float hilimit1,
                                @Param(value = "lolimit1") Float lolimit1,
                                @Param(value = "hilimit2") Float hilimit2,
                                @Param(value = "lolimit2") Float lolimit2,
                                @Param(value = "hilimit3") Float hilimit3,
                                @Param(value = "lolimit3") Float lolimit3,
                                @Param(value = "hilimit4") Float hilimit4,
                                @Param(value = "stander") Float stander,
                                @Param(value = "controlenable") Integer controlenable);
    List<TCfgMete> selectByPage(TCfgMete tCfgMete);

    int batchInsert(List<TCfgMete> list);
}
