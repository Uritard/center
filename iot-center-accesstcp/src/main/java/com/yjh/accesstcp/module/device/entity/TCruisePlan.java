package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TCruisePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long planId;

    private String planName;

}
