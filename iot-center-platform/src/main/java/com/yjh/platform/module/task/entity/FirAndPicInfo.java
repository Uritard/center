package com.yjh.platform.module.task.entity;


import lombok.Data;

import java.io.Serializable;
@Data
public class FirAndPicInfo implements Serializable {

    private Long cruiseDataId;

    private String firName;

    private String firPath;

    private String picPath;

   private String dateTime;
}
