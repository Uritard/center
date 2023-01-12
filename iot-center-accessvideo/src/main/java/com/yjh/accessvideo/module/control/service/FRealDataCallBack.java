package com.yjh.accessvideo.module.control.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.hik.PlayCtrl;

public class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {

    private NativeLong m_lPort =  new NativeLong(-1);
    private static PlayCtrl playCtrl = PlayCtrl.INSTANCE;

    //预览回调
    @Override
    public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser)
    {
        switch (dwDataType)
        {
            case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                if (!playCtrl.PlayM4_GetPort(new NativeLongByReference(m_lPort))) //获取播放库未使用的通道号
                {
                    break;
                }

                if (dwBufSize > 0)
                {
                    if (!playCtrl.PlayM4_SetStreamOpenMode(m_lPort, PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                    {
                        break;
                    }

                    if (!playCtrl.PlayM4_OpenStream(m_lPort, pBuffer, dwBufSize, 1024 * 1024)) //打开流接口
                    {
                        break;
                    }

                    if (!playCtrl.PlayM4_Play(m_lPort, null)) //播放开始
                    {
                        break;
                    }
                }
            case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                if ((dwBufSize > 0) && (m_lPort.intValue() != -1))
                {
                    if (!playCtrl.PlayM4_InputData(m_lPort, pBuffer, dwBufSize))  //输入流数据
                    {
                        break;
                    }
                }
        }
    }
}
