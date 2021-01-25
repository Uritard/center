package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2021-01-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDiagnosePlanDetail对象", description = "")
public class TDiagnosePlanDetail implements Serializable {

    private static final long serialVersionUID = 1L;


    private String diagnosePlanId;

    private String signalOpt;

    private String blurOpt;

    private String contrastOpt;

    private String brightOpt;

    private String darkOpt;

    private String chromaOpt;

    private String monoOpt;

    private String noiseOpt;

    private String streakOpt;

    private String freezeOpt;

    private String shakeOpt;

    private String flashOpt;

    private String sceneOpt;

    private String coverOpt;

    private String ptzOpt;


}
