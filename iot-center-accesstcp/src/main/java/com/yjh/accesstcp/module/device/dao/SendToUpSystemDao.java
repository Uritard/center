package com.yjh.accesstcp.module.device.dao;



import com.yjh.accesstcp.module.device.entity.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface SendToUpSystemDao {
    List<Map<String,Object>> selectDeviceModel();
    List<Map<String,Object>> selectRobotInfo();

}
