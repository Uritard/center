
# 常见问题列表

## iot-center-accessvideo
### Windows下服务因为无法加载海康SDK无法启动
#### 方法1
在IDEA的Run/Debug Configurations页面配置VM options，增加一行
```
-Djna.library.path=D:\yijiahe\projects\Z100\HKSDK\CH-HCNetSDKV5.3.6.30_build20180816_Win64\lib
```

上述路径是海康SDK的存放路径，里面包含了HCNetSDK.dll等库文件

## 声纹设备
### 音频测试报文
```
FC FC FC FC 7F 00 00 00 2A 00 00 00 00 00 00 00 01 FF FF FF FF FF FF 01 02 03 04 05 06 01 02 03 01 00 00 FF FF FF 00 0A BB 80 10 02 01 01 02 02 00 ED FD
```