package com.yjh.platform.common;

public class Constant {

    public static final String account_lock_times = "account_lock_times:userAccountID";

    public static final String SET_PRESET_URL = "http://iot-center-accessvideo/camera/v1/setPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CANCEL_PRESET_URL = "http://iot-center-accessvideo/camera/v1/cancelPreset?cameraId={cameraId}&presetId={presetId}";

    public static final String CAPTURE_PRESET_URL = "http://iot-center-accessvideo/camera/v1/capturePresetPicture?cameraId={cameraId}&presetId={presetId}";

    public static String taskId;

}
