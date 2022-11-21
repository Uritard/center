package com.yjh.accessudp.netty.server;

import com.yjh.accessudp.common.Constant;
import com.yjh.accessudp.module.device.entity.XMLBaseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/8/24
 **/
@Slf4j
public class LinkageUploadThread implements Runnable{

    private final String meteId;
    private final String meteKind;
    private final String value;
    private final String commit;
    private final String time;
    public LinkageUploadThread(String meteId, String meteKind, String value, String commit, String time){
        this.meteId = meteId;
        this.meteKind = meteKind;
        this.value = value;
        this.commit = commit;
        this.time = time;
    }
    @Override
    public void run() {
        try {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>(5);
            xmlBaseModel.setType("6");
            //联动点位编码 对应主辅系统监控索引号
            xmlItem.put("source_code", meteId);
            //信号类型 0，遥控；1，遥信；2，遥测
            xmlItem.put("source_type", meteKind);
            //属性
            xmlItem.put("source_attr", value);
            //值描述
            xmlItem.put("source_value", commit);
            //事件时标
            xmlItem.put("source_time", time);
            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> cruiseResult = new HashMap<>(2);
            cruiseResult.put("list", list);
            log.info("联动信号上送：-" + cruiseResult);
            Constant.otherServer(cruiseResult, Constant.TCP_URL);
        }catch (Exception e){
            log.error("联动信号上送失败", e);
        }
    }
}
