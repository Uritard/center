package com.yjh.platform.module.device.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.entity.TMeter;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2023/10/20
 */
@Component
public class MeterInfoUpload {

    @Autowired
    private TMeterDao tMeterDao;

    //定时将电表信息上传给上级系统
//    @Scheduled(cron = "0 */30 * * * ?")
    public void uploadMeteInfo(){
        if (!Constant.upSystemFlag()) {
            return;
        }
        List<TMeter> meterList = tMeterDao.selectAll();
        if (meterList.isEmpty()){
            return;
        }
        Map<String,List<XMLBaseModel>> map = new HashMap<>();
        List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String,Object>> itemList = new ArrayList<>();

        xmlBaseModel.setItems(itemList);
        xmlBaseModel.setType("meter");
        xmlBaseModelList.add(xmlBaseModel);
        map.put("list",xmlBaseModelList);

        for(TMeter meter:meterList){
            Map<String,Object> item = new HashMap<>();
            item.put("name",meter.getName());
            item.put("ip",meter.getIp());
            item.put("port",meter.getPort());
            item.put("address",meter.getAddress());
            item.put("upRegionId",meter.getUpRegionId());
            item.put("totalPositivePower",meter.getTotalPositivePower());
            item.put("totalPositiveReactivePower",meter.getTotalPositiveReactivePower());
            item.put("totalNegativePositivePower",meter.getTotalNegativePositivePower());
            item.put("collectPowerTime", DateTimeUtil.format(meter.getCollectPowerTime()));
            itemList.add(item);
        }
        Constant.otherServer(map, Constant.TCP_URL);
    }
}
