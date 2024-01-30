package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2024/1/30
 * @since [产品/模块版本] （可选）
 */
public enum DataType {
    /**
     * 正向有功总电能数据类型
     */
    POSITIVE_POWER_TOTAL {
        @Override
        public byte[] getDataTypeValue(String protocol) {
            return "1997".equals(protocol) ? Constant.DATA_TYPE_POSITVICE_POWER_TOTAL : Constant.DATA_TYPE_POSITVICE_POWER_TOTAL_2007;
        }
    },
    /**
     * 正向有功总电能数据类型
     */
    POSITIVE_REACTIVE_POWER_TOTAL {
        @Override
        public byte[] getDataTypeValue(String protocol) {
            return "1997".equals(protocol) ? Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL : Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL_2007;
        }
    },
    /**
     * 正向有功总电能数据类型
     */
    NEGATIVE_REACTIVE_POWER_TOTAL {
        @Override
        public byte[] getDataTypeValue(String protocol) {
            return "1997".equals(protocol) ? Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL : Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL_2007;
        }
    };

    public abstract byte[] getDataTypeValue(String protocol);

}

