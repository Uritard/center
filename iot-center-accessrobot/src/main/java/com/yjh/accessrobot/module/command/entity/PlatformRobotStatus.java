package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

@Data
public class PlatformRobotStatus {
    private String netStatus;

    public PlatformRobotStatus(){
        netStatus = "";
    }
}
