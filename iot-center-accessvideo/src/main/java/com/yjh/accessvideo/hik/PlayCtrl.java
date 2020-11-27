package com.yjh.accessvideo.hik;

import com.sun.jna.*;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

//播放库函数声明,PlayCtrl.dll
public interface PlayCtrl extends Library
{
    PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("PlayCtrl",
            PlayCtrl.class);

//    PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("/home/yjh_iot_center/iot-center-accessvideo-1.0.0/config/lib/libPlayCtrl.so",
//            PlayCtrl.class);

    public static final int STREAME_REALTIME = 0;
    public static final int STREAME_FILE = 1;

    boolean PlayM4_GetPort(NativeLongByReference nPort);
    boolean PlayM4_OpenStream(NativeLong nPort, ByteByReference pFileHeadBuf, int nSize, int nBufPoolSize);
    boolean PlayM4_InputData(NativeLong nPort, ByteByReference pBuf, int nSize);
    boolean PlayM4_CloseStream(NativeLong nPort);
    boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);
    boolean PlayM4_Play(NativeLong nPort, W32API.HWND hWnd);
    boolean PlayM4_Stop(NativeLong nPort);
    boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);
    boolean PlayM4_SetOverlayPriInfoFlag(long nPort, int nIntelType, boolean bTrue);
    boolean PlayM4_GetJPEG(long nPort, byte[] pJpg, int nbufSize, IntByReference pJpgSize);

    //第一个接口为测温主类型接口，开启关闭测温的大开关；nIntelType：对应为0x20；bTrue：0表示关闭，1表示开启
    boolean PlayM4_RenderPrivateData(long nPort, int nIntelType, int bTrue);
    boolean PlayM4_RenderPrivateDataEx(long nPort, int nIntelType, int nSubType, int bTrue);
    int PlayM4_GetLastError();

    //图片大小
//    public static class NET_DVR_JPEGSIZE extends Structure {
//        public byte[] lpJpeg = new byte[800*600*3/2];/* 存放JEPG图像数据地址，由用户分配，不得小于JPEG图像大小，建议大小w * h * 3/2， 其中w和h分别为图像宽高 */
//    }

}