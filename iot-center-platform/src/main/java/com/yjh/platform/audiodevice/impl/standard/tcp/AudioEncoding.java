package com.yjh.platform.audiodevice.impl.standard.tcp;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public enum AudioEncoding {
    /**
     * PCM编码
     */
    PCM(0),

    /**
     * G711A编码
     */
    G711A(1);

    private int val;

    AudioEncoding(int val) {
        this.val = val;
    }

    public static AudioEncoding getInstance(int val) {
        switch (val) {
            case 0:
                return PCM;
            case 1:
                return G711A;
            default:
                throw new IllegalArgumentException("Unknown AudioEncoding: " + val);
        }
    }
}
