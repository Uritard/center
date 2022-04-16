package com.yjh.accessvideo.common.mqtt.alarmMsgBody;

import lombok.Data;

import java.util.List;

@Data
public class AlarmDetail {
     String  bay_name;
     String  device_name;
     String  point_name;
     String  time;
     String  pic_width;
     String  pic_height;
     String  pic_raw;
     String  pic_defect;
     String  pic_different;
     String  pic_diff_base;
     List<Defect> defect;
     List<Different> different;

}
