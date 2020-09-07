package com.yjh.platform.module.user.dao;

import java.util.List;
import java.util.Date;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TAlgorithmConfDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-09-07
 */
@Repository
public interface TAlgorithmConfDao {

    int add(TAlgorithmConf tAlgorithmConf);
    int deleteByPrimaryId(@Param(value = "presetId") Long presetId);
    int update(TAlgorithmConf tAlgorithmConf);
    TAlgorithmConf selectByPrimaryId(@Param(value = "presetId") Long presetId);
    List<TAlgorithmConf> select(@Param(value = "presetId") Long presetId,
                                @Param(value = "algorithmId") Long algorithmId,
                                @Param(value = "configName") String configName,
                                @Param(value = "status") Integer status,
                                @Param(value = "ifDel") Integer ifDel,
                                @Param(value = "ifShow") Integer ifShow,
                                @Param(value = "picUrl") String picUrl,
                                @Param(value = "applyModule") Integer applyModule,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime);
    List<TAlgorithmConfDetail> selectByPage(TAlgorithmConf tAlgorithmConf);

    int batchAdd(List<TAlgorithmConf> list);
}
