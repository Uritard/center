package com.yjh.accessudp.module.device.dao;



import com.yjh.accessudp.module.device.entity.SYAllInfo;
import com.yjh.accessudp.module.device.entity.TCfgDataCurrent;
import com.yjh.accessudp.module.device.entity.THisSignalData;
import com.yjh.accessudp.module.device.entity.THisTelemeterData;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgMeteDao {

    int insertForMete(SYAllInfo syAllInfo);
    int updateForMete(SYAllInfo syAllInfo);

    int insertForDevice(SYAllInfo syAllInfo);
    int updateForDevice(SYAllInfo syAllInfo);

    int insertForTeleadjust(SYAllInfo syAllInfo);
    int updateForTeleadjust(SYAllInfo syAllInfo);

    int insertForTelecontrol(SYAllInfo syAllInfo);
    int updateForTelecontrol(SYAllInfo syAllInfo);

    int insertForTelemeter(SYAllInfo syAllInfo);
    int updateForTelemeter(SYAllInfo syAllInfo);

    int insertForTelesignal(SYAllInfo syAllInfo);
    int updateForTelesignal(SYAllInfo syAllInfo);

    String selectByMeteId(String meteId);
    TCfgDataCurrent selectByPrimaryIdTCfgDataCurrent(Long meteId);
    int insertTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent);
    int updateTCfgDataCurrent(TCfgDataCurrent tCfgDataCurrent);
    int deleteForDeviceAll();
    int deleteForMeteAll();
    int deleteForTeleadjustAll();
    int deleteForTelecontrolAll();
    int deleteForTelemeterAll();
    int deleteForTelesignalAll();

    int insertIntoTHisSignalData(THisSignalData tHisSignalData);
    int insertIntoTHisTelemeterData(THisTelemeterData tHisTelemeterData);
}
