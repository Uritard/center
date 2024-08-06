package com.yjh.platform.module.config.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelInfo implements Serializable {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("label")
    private String label;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("width")
    private Integer width;
    @JsonProperty("name")
    private String name;
}
