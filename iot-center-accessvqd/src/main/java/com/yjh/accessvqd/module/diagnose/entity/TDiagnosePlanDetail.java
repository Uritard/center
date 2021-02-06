package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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

    @TableField(value = "diagnose_plan_id",updateStrategy = FieldStrategy.IGNORED)
    private String diagnosePlanId;
     @TableField(value = "signal_opt",updateStrategy = FieldStrategy.IGNORED)
    private String signalOpt;
    @TableField(value = "blur_opt",updateStrategy = FieldStrategy.IGNORED)
    private String blurOpt;
    @TableField(value = "contrast_opt",updateStrategy = FieldStrategy.IGNORED)
    private String contrastOpt;
    @TableField(value = "bright_opt",updateStrategy = FieldStrategy.IGNORED)
    private String brightOpt;
    @TableField(value = "dark_opt",updateStrategy = FieldStrategy.IGNORED)
    private String darkOpt;
    @TableField(value = "chroma_opt",updateStrategy = FieldStrategy.IGNORED)
    private String chromaOpt;
    @TableField(value = "mono_opt",updateStrategy = FieldStrategy.IGNORED)
    private String monoOpt;
    @TableField(value = "noise_opt",updateStrategy = FieldStrategy.IGNORED)
    private String noiseOpt;
     @TableField(value = "streak_opt",updateStrategy = FieldStrategy.IGNORED)
    private String streakOpt;
    @TableField(value = "freeze_opt",updateStrategy = FieldStrategy.IGNORED)
    private String freezeOpt;
    @TableField(value = "shake_opt",updateStrategy = FieldStrategy.IGNORED)
    private String shakeOpt;
    @TableField(value = "flash_opt",updateStrategy = FieldStrategy.IGNORED)
    private String flashOpt;
    @TableField(value = "scene_opt",updateStrategy = FieldStrategy.IGNORED)
    private String sceneOpt;
    @TableField(value = "cover_opt",updateStrategy = FieldStrategy.IGNORED)
    private String coverOpt;
    @TableField(value = "ptz_opt",updateStrategy = FieldStrategy.IGNORED)
    private String ptzOpt;


}
