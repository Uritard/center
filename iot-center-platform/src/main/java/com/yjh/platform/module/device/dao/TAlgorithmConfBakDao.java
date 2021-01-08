package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.device.entity.TAlgorithmConfBak;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-12-22
 */
@Repository
public interface TAlgorithmConfBakDao {

    int add(TAlgorithmConfBak tAlgorithmConfBak);
    int deleteByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    int update(TAlgorithmConfBak tAlgorithmConfBak);
    TAlgorithmConfBak selectByPrimaryId(@Param(value = "deviceMeteId") Long deviceMeteId);
    List<TAlgorithmConfBak> select(@Param(value = "deviceMeteId") Long deviceMeteId,
                                @Param(value = "algorithmId") Long algorithmId,
                                @Param(value = "configName") String configName,
                                @Param(value = "status") Integer status,
                                @Param(value = "ifDel") Integer ifDel,
                                @Param(value = "ifShow") Integer ifShow,
                                @Param(value = "picUrl") String picUrl,
                                @Param(value = "applyModule") Integer applyModule,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime);
    List<TAlgorithmConfBak> selectByPage(TAlgorithmConfBak tAlgorithmConfBak);

    int batchAdd(List<TAlgorithmConfBak> list);
    int batchDelete(List<String> list);
    TAlgorithmInfo selectByAnalyseType(@Param(value = "analyseType") String analyseType);
    List<TDictBusiness> selectAnalyseType();

}
