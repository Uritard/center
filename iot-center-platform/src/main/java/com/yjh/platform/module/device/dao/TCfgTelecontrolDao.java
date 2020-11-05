package com.yjh.platform.module.device.dao;

import java.util.List;

import com.yjh.platform.module.device.entity.SYAllInfo;
import com.yjh.platform.module.device.entity.TCfgTelecontrol;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgTelecontrolDao {

    int insert(TCfgTelecontrol tCfgTelecontrol);
    int deleteByPrimaryId(@Param(value = "deviceId") String deviceId);
    int update(TCfgTelecontrol tCfgTelecontrol);
    TCfgTelecontrol selectByPrimaryId(@Param(value = "deviceId") String deviceId);
    List<TCfgTelecontrol> select(@Param(value = "deviceId") String deviceId,
                                @Param(value = "meteId") String meteId,
                                @Param(value = "meteName") String meteName,
                                @Param(value = "meteIndex") Integer meteIndex,
                                @Param(value = "meteCid") Integer meteCid,
                                @Param(value = "controlStatus") Integer controlStatus,
                                @Param(value = "enableString") String enableString,
                                @Param(value = "succeedString") String succeedString,
                                @Param(value = "triggerString") String triggerString,
                                @Param(value = "controlValue") Integer controlValue,
                                @Param(value = "meteCode") String meteCode,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "description") String description,
                                @Param(value = "describer") String describer);
    List<TCfgTelecontrol> selectByPage(TCfgTelecontrol tCfgTelecontrol);

    int batchInsert(List<TCfgTelecontrol> list);
    int insertForAll(SYAllInfo syAllInfo);
    int updateForAll(SYAllInfo syAllInfo);
}
