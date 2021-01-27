package com.yjh.accessvideo.common.logs.track;

import com.yjh.accessvideo.commons.utils.uid.UniqueID;

import java.util.Map;

/**
 * @author lichensi
 * @date 2020/12/9 16:59
 */
public interface HttpTracing extends TrackHelper {

    default String newRoute() {
        String routeTraceId = UniqueID.getInstance(UniqueID.Type.IP).ID();
        this.setRoute(routeTraceId);
        return routeTraceId;
    }

    default void setRoute(String routeTraceId) {
        this.setRouteTrackInfo(Tags.TRACE_ID.getName(), routeTraceId);
        MDCHelper.put(Tags.TRACE_ID.getName(), routeTraceId);
    }

    default String removeRoute() {
        String result = this.getRouteTraceId();
        try {
            removeRouteTrackInfo();
        } finally {
            MDCHelper.remove(Tags.TRACE_ID.getName());
        }
        return result;
    }


    default String getRouteTraceId() {
        return this.getRouteTrackInfo(Tags.TRACE_ID.getName());
    }

    default void setHttpRouteRequestId(String httpRequestId) {
        this.setRouteTrackInfo(Tags.REQUEST_ID.getName(), httpRequestId);
    }

    default void setHttpRouteRequestIp(String httpRequestIp) {
        this.setRouteTrackInfo(Tags.REQUEST_IP.getName(), httpRequestIp);
    }

    default void setHttpRouteRequestOrigin(String httpRequestOrigin) {
        this.setRouteTrackInfo(Tags.REQUEST_ORIGIN.getName(), httpRequestOrigin);
    }

    default void setHttpRouteRequestMethod(String httpRequestMethod) {
        this.setRouteTrackInfo(Tags.REQUEST_METHOD.getName(), httpRequestMethod);
    }

    default void setHttpRouteRequestPath(String httpRequestPath) {
        this.setRouteTrackInfo(Tags.REQUEST_PATH.getName(), httpRequestPath);
    }

    static void setHttpRouteTrackInfo(Map<String, String> trackInfo){
        TrackHelper.setCurrentTrackInfo(trackInfo);
    }

    static String getCurrentTraceID() {
        return TrackHelper.getCurrentTrackInfo(Tags.TRACE_ID.getName());
    }

    static String getRouteRequestId() {
        return TrackHelper.getCurrentTrackInfo(Tags.REQUEST_ID.getName());
    }

    static String getRouteRequestIp() {
        return TrackHelper.getCurrentTrackInfo(Tags.REQUEST_IP.getName());
    }

    static String getRouteRequestOrigin() {
        return TrackHelper.getCurrentTrackInfo(Tags.REQUEST_ORIGIN.getName());
    }

    static String getRouteRequestMethod() {
        return TrackHelper.getCurrentTrackInfo(Tags.REQUEST_METHOD.getName());
    }

    static String getRouteRequestPath() {
        return TrackHelper.getCurrentTrackInfo(Tags.REQUEST_PATH.getName());
    }

    static Map<String, String> getHttpRouteTrackInfo() {
        return TrackHelper.getCurrentTrackInfo();
    }

    static void removeHttpRouteTrackInfo() {
        try {
            TrackHelper.removeTrackInfo();
        } finally {
            MDCHelper.remove(Tags.TRACE_ID.getName());
        }
    }

    enum Tags {

        REQUEST_IP("REQUEST_IP"),
        REQUEST_ID("REQUEST_ID"),
        TRACE_ID("TRACE_ID"),
        REQUEST_METHOD("REQUEST_METHOD"),
        REQUEST_ORIGIN("REQUEST_ORIGIN"),
        REQUEST_PATH("REQUEST_PATH"),
        USER_ID("USER_ID"),
        USER_NAME("USER_NAME");

        String name;

        Tags(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
