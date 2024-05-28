package com.yjh.platform.module.patrol.entity.enums;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/05/24
 */
public enum VoiceType {
    PD("pd","局部放电"),
    SUSPEND_PD("suspend_pd","悬浮放电"),
    SURFACE_PD("surface_pd","沿面放电"),
    CORONA_PD("corona_pd","电晕放电"),
    SUSPEND_SURFACE_PD("suspend_surface_pd","悬浮，悬浮放电"),
    DHFD("dhfd","电弧放电"),
    BYQ_ZLPC("byq_zlpc","变压器直流偏磁"),
    BYQ_ZGZ("byq_zgz","变压器重过载"),
    BYQ_DLCJ("byq_dlcj","变压器短路冲击"),
    BYQ_ZBJ_LOOSENESS("byq_zbj_looseness","变压器组部件松动"),
    BYQ_LQQYX("byq_lqqyx","变压器冷却器异响")
        ;


    VoiceType(String type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }


    private String type;

    private String typeName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName(String type){
        return typeName;
    }

    public static List<String> getAllType(){
        List<String> re = new ArrayList<>();
        for (VoiceType item : VoiceType.values()){
            re.add(item.getType());
        }
        return re;
    }

}
