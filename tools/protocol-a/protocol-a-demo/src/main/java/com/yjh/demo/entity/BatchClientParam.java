package com.yjh.demo.entity;

import lombok.Data;

import java.util.List;

/**
 * @ClassName: BatchTaskParam
 * @Description:
 * @author: yanhao
 * @date: 2022/9/7
 */
@Data
public class BatchClientParam {

    private String receiveCode;

    private List<String> sendCodeList;

    private String ip;

    private int port;

}
