package com.yjh.platform.audiodevice;

/**
 * 录音设备接口
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public interface AudioDevice {

    /**
     * 获取设备ID
     * @return
     */
    String getDeviceId();
    /**
     * 开始录音
     *
     * @throws Exception
     */
    void startRecording() throws Exception;

    /**
     * 查询是否在录音
     *
     * @return
     */
    boolean isRecording();

    /**
     * 停止当前录音，但是不保存文件
     *
     * @throws Exception
     */
    void stopRecording() throws Exception;

    /**
     * 停止当前录音，并且保存录音文件到指定路径
     *
     * @param audioFilepath 音频文件保存路径
     * @throws Exception
     */
    void stopRecordingAndSave(String audioFilepath) throws Exception;

}
