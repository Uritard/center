package com.yjh.demo.entity;


import lombok.Data;

/**
 * @ClassName: BatchMessageParam
 * @Description:
 * @author: yanhao
 * @date: 2022/8/27
 */
@Data
public class BatchMessageParam {

    private String filePath;
    private String remoteFilename;
    private String ip;
    private int ftpPort;
    private String keyPw;
    private String username;
    private String password;
    private String xml;
    private int socketPort;
    private int taskCount;
    private int threadCount;
}
