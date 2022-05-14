package com.yjh.accessudp.module.device.dao;



import com.yjh.accessudp.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgMeteDao {

    int insertForMete(List<SYAllInfo> list);
    int updateForMete(SYAllInfo syAllInfo);

    int insertForDevice(List<SYAllInfo> list);
    int updateForDevice(SYAllInfo syAllInfo);

    int insertForTeleadjust(List<SYAllInfo> list);
    int updateForTeleadjust(SYAllInfo syAllInfo);

    int insertForTelecontrol(List<SYAllInfo> list);
    int updateForTelecontrol(SYAllInfo syAllInfo);

    int insertForTelemeter(List<SYAllInfo> list);
    int updateForTelemeter(SYAllInfo syAllInfo);

    int insertForTelesignal(List<SYAllInfo> list);
    int updateForTelesignal(SYAllInfo syAllInfo);

    String selectByMeteId(String meteId);
    TCfgDataCurrent selectByPrimaryIdTCfgDataCurrent(Long meteId);
    int insertTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent);
    int updateTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent);
    int deleteForDeviceAll(List<SYAllInfo> list);
    int deleteForMeteAll(List<SYAllInfo> list);
    int deleteForTeleadjustAll(List<SYAllInfo> list);
    int deleteForTelecontrolAll(List<SYAllInfo> list);
    int deleteForTelemeterAll(List<SYAllInfo> list);
    int deleteForTelesignalAll(List<SYAllInfo> list);

    int insertIntoTHisSignalData(THisSignalData tHisSignalData);
    int insertIntoTHisTelemeterData(THisTelemeterData tHisTelemeterData);

    TSysParam selectByParamType(@Param(value = "paramCode") String paramCode);
}
