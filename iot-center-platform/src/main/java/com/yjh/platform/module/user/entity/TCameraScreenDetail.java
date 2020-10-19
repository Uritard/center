package com.yjh.platform.module.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2020/10/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TCameraScreenDetail extends TCameraScreen{
    List<Object> cameraList;
}
