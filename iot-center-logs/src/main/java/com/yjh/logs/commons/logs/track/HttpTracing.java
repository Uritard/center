package com.yjh.logs.commons.logs.track;


import com.yjh.logs.common.utils.uid.UniqueID;

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
        this.setRouteAttachment(Tags.TRACE_ID.getName(), routeTraceId);
        MDCHelper.put(Tags.TRACE_ID.getName(), routeTraceId);
    }

    default String removeRoute() {
        String result = this.getRouteTraceId();
        try {
            removeRouteAttachment();
        } finally {
            MDCHelper.remove(Tags.TRACE_ID.getName());
        }
        return result;
    }


    default String getRouteTraceId() {
        return this.getRouteAttachment(Tags.TRACE_ID.getName());
    }

    default void setRouteRequestId(String httpRequestId) {
        this.setRouteAttachment(Tags.REQUEST_ID.getName(), httpRequestId);
    }

    default void setRouteRequestIp(String httpRequestIp) {
        this.setRouteAttachment(Tags.REQUEST_IP.getName(), httpRequestIp);
    }

    default void setRouteRequestOrigin(String httpRequestOrigin) {
        this.setRouteAttachment(Tags.REQUEST_ORIGIN.getName(), httpRequestOrigin);
    }

    default void setRouteRequestMethod(String httpRequestMethod) {
        this.setRouteAttachment(Tags.REQUEST_METHOD.getName(), httpRequestMethod);
    }

    default void setRouteRequestPath(String httpRequestPath) {
        this.setRouteAttachment(Tags.REQUEST_PATH.getName(), httpRequestPath);
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
