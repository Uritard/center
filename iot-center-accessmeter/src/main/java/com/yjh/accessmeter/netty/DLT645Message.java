package com.yjh.accessmeter.netty;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Data
public class DLT645Message {

    /**
     * 控制码
     */
    private byte controlCode;

    /**
     * 电表地址
     */
    private String address;

    /**
     * 数据域
     */
    private byte[] data;

}
