package com.yjh.accesstcp.module.device.entity;

import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author YIJIAHE
 */
public class DeferredResultEx<T> {

    private DeferredResult<T> deferredResult;

    private boolean waited;

    public DeferredResultEx(DeferredResult<T> result) {
        this.deferredResult = result;
    }

    public DeferredResult<T> getDeferredResult() {
        return deferredResult;
    }

    public void setDeferredResult(DeferredResult<T> deferredResult) {
        this.deferredResult = deferredResult;
    }

    public boolean isWaited() {
        return waited;
    }

    public void setWaited(boolean waited) {
        this.waited = waited;
    }
}
