package com.yjh.video.core.service.bean;

import com.yjh.video.core.gb28181.bean.Device;
import com.yjh.video.core.gb28181.bean.msg.WVPResult;
import org.springframework.web.context.request.async.DeferredResult;

public class PlayResult {

    private DeferredResult<WVPResult<String>> result;
    private String uuid;

    private Device device;

    public DeferredResult<WVPResult<String>> getResult() {
        return result;
    }

    public void setResult(DeferredResult<WVPResult<String>> result) {
        this.result = result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
