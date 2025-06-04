package com.yjh.platform.common.utils;

import com.yjh.video.api.result.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class CommonFutureUtil {

    private static final Map<String, CompletableFuture<Result<?>>> COMPLETABLE_FUTURE_MAP = new ConcurrentHashMap<>();

    /**
     * 注册异步回调，返回 CompletableFuture 供后续处理
     * @param messageId 发送消息Id
     * @param <R>     泛型
     * @return 异步后续处理对象
     */
    public static <R> CompletableFuture<Result<R>> registerFuture(String messageId) {
        CompletableFuture<?> future = COMPLETABLE_FUTURE_MAP.computeIfAbsent(messageId, k -> {
            CompletableFuture<com.yjh.video.api.result.Result<?>> f = new CompletableFuture<>();
            return f.exceptionally(ex -> {
                COMPLETABLE_FUTURE_MAP.remove(messageId);
                log.error(ex.getMessage(), ex);
                if (ex instanceof TimeoutException) {
                    return com.yjh.video.api.result.Result.error(com.yjh.video.api.result.ResultCodeEnum.ERROR504);
                }
                return com.yjh.video.api.result.Result.error(com.yjh.video.api.result.ResultCodeEnum.ERROR500);
            });
        });
        // 确保我们返回的是正确的类型
        @SuppressWarnings("unchecked") CompletableFuture<com.yjh.video.api.result.Result<R>> typedFuture =
            (CompletableFuture<com.yjh.video.api.result.Result<R>>)future;
        return typedFuture;
    }

    /**
     * 对异步返回结果做处理，触发异步回调逻辑
     * @param messageId    请求ID，可以认为是 sessionId
     * @param futureResult 异步返回结果
     * @param <R>          泛型
     * @return 1: 回调成功  0: 回调失败  -1: 没有注册回调
     */
    public static <R> int futureComplete(String messageId, Result<R> futureResult) {
        CompletableFuture<Result<?>> completableFuture = COMPLETABLE_FUTURE_MAP.remove(messageId);
        if (completableFuture != null) {
            boolean ret = completableFuture.complete(futureResult);
            return ret ? 1 : 0;
        } else {
            return -1;
        }
    }

    /**
     *  移除异步回调
     * @param messageId  请求ID，可以认为是 sessionId
     */
    public static void removeFuture(String messageId) {
        COMPLETABLE_FUTURE_MAP.remove(messageId);
    }
}
