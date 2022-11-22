package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TCfgUnionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long ruleId;

    private Long planId;

    private String ruleName;

    private String inputParam;

}
