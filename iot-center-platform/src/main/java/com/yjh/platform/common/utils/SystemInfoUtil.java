package com.yjh.platform.common.utils;

import java.io.*;
import java.util.*;

/**
 * @author lqh
 * @since 2020/10/20
 */
public class SystemInfoUtil {

        /**
         * 获取cpu使用情况
         * @return
         * @throws Exception
         */
//        public List<Map<String,Object>> getcpuUsage() throws Exception {
        public List<String> getcpuUsage() throws Exception {
            //List<Map<String,Object>> result = new ArrayList<>();
            List<String> result = new ArrayList<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("top -b -n 1");// 调用系统的“top"命令
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                String[] strArray = null;
                while ((str = in.readLine()) != null) {
   //                     strArray = str.split(" ");
//                            Map<String,Object> map = new HashMap<>();
//                            map.put("Pid",strArray[0]);
//                            map.put("Command",strArray[11]);
//                            map.put("State",strArray[7]);
//                            map.put("cpu",strArray[9]);
                            result.add(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
            return result;
        }
        /**
         * 内存监控
         * @return
         * @throws Exception
         */
        //public Map<String,Object> getMemUsage() throws Exception {
        public List<String> getMemUsage() throws Exception {
            List<String> map = new ArrayList<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("free");// 调用系统的“free"命令
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                String[] strArray = null;
                while ((str = in.readLine()) != null) {
//                    strArray = str.split(" ");
//                    map.put("total",strArray[0]);
//                    map.put("used",strArray[1]);
//                    map.put("free",strArray[2]);
                    map.add(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
            return map;
        }

        /**
         * 获取磁盘空间大小
         *
         * @return
         * @throws Exception
         */
        //public List<Map<String,Object>> getDeskUsage() throws Exception {
        public List<String> getDeskUsage() throws Exception {
            List<String> result = new LinkedList<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("df -hl");//df -hl 查看硬盘空间
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                String[] strArray = null;
                while ((str = in.readLine()) != null) {
//                    int m = 0;
//                    strArray = str.split(" ");
//                    Map<String,Object> map = new HashMap<>();
//                    map.put("Pid",strArray[0]);
//                    map.put("read",strArray[2]);
//                    map.put("write",strArray[3]);
//                    map.put("all",Long.parseLong(strArray[2])+Long.parseLong(strArray[3]));
                    result.add(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
            return result;
        }



    }