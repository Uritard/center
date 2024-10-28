package com.yjh.accessmeter.protocol.aigateway.info;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@lombok.Data
public class Data {
    private DataListInfo dataList;
    private String type;
    private String gatewayId;
    private String timestamp;

    @lombok.Data
    public static class DataListInfo{
        private IotDeviceType signal;
        private IotDeviceType measure;
        private IotDeviceType pulse;
    }

    @lombok.Data
    public static class IotDeviceType{
        List<DataInfo> data;
    }

    @lombok.Data
    public static class DataInfo{
        private String dotName;
        private String nodeId;
        private String value;
    }
}
