package com.yjh.platform.common.logs;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author lichensi
 * @date 2020/12/10 17:58
 */
public class OperateLogEvent extends ApplicationEvent {

    private OperateLogDto operateLogDto;

    private Map<String,String> trackInfo;

    public OperateLogEvent(OperateLogDto operateLogDto, Map<String,String> trackInfo, Object source) {
        super(source);
        this.operateLogDto = operateLogDto;
        this.trackInfo = trackInfo;
    }

    public void setOperateLogDto(OperateLogDto operateLogDto) {
        this.operateLogDto = operateLogDto;
    }

    public OperateLogDto getOperateLogDto() {
        return operateLogDto;
    }

    public Map<String, String> getTrackInfo() {
        return trackInfo;
    }

    public void setTrackInfo(Map<String, String> trackInfo) {
        this.trackInfo = trackInfo;
    }
}
