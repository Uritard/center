package com.yjh.platform.common.utils;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @author lqh
 * @since 2020/10/20
 */
public class SystemInfoUtil {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SystemInfoUtil.class);
    DecimalFormat df   = new DecimalFormat("########0.00");

    /**
         * 获取cpu使用情况
         * @return
         * @throws Exception
         */
        public List<Map<String,String>> getCpuUsage() throws Exception {
            List<Map<String,String>> result = new ArrayList<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("top -bn 1");// 调用系统的“top"命令
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                int i = 1;
                String[] strArray = null;
                while ((str = in.readLine()) != null) {
                    if(i > 7){
                        strArray = str.split("\\s+");
                        if("".equals(strArray[0])){
                            strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                        }
                        Map<String,String> map = new HashMap<>();
                        map.put("pid",strArray[0]);
                        map.put("command",strArray[11]);
                        map.put("state",strArray[7]);
                        map.put("cpu",strArray[8]);
                        result.add(map);
                    }
                   i++;
                }

            } catch (Exception e) {
                log.error("获取cpu信息错误:", e);
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
        public Map<String,String> getMemUsage() throws Exception {
            Map<String,String> map = new HashMap<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("free -m");// 调用系统的“free"命令
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                String[] strArray = null;
                str = in.readLine();
                if ((str = in.readLine()) != null) {
                    strArray = str.split("\\s+");
                    map.put("total",strArray[1]);
                    map.put("used",strArray[2]);
                    map.put("free",strArray[3]);
                    Long other = Long.parseLong(strArray[1])-Long.parseLong(strArray[2])-Long.parseLong(strArray[3]);
                    map.put("other",other.toString());
                }
            } catch (Exception e) {
                log.error("获取内存信息错误:", e);
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
        public List<Map<String,String>> getDeskUsage() throws Exception {
            List<Map<String,String>> result = new LinkedList<>();
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("pidstat -d");//df -hl 查看硬盘空间
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                String[] strArray = null;
                int i = 1;
                while ((str = in.readLine()) != null) {
                    if(i > 3){
                        strArray = str.split("\\s+");
                        if("".equals(strArray[0])){
                            strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                        }
                        Map<String,String> map = new HashMap<>();
                        map.put("pid",strArray[2]);
                        map.put("read",strArray[3]);
                        map.put("write",strArray[4]);
                        Double all = Double.valueOf(strArray[3])+Double.valueOf(strArray[4]);
                        map.put("all",df.format(all));
                        result.add(map);
                    }
                    i++;
                }
            } catch (Exception e) {
                log.error("获取硬盘信息错误:", e);
            } finally {
                in.close();
            }
            return result;
        }

    /**
     * 获取cpu使用百分比
     *
     * @return
     * @throws Exception
     */
    public double getCpuOnUse() throws Exception {
        Map<String,String> map = new HashMap<>();
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("top -b -n 1");// 调用系统的“top"命令
        BufferedReader in = null;
        double re = -1;
        try {
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str = null;
            int i = 1;
            String[] strArray = null;
            while ((str = in.readLine()) != null) {
                if(i == 3){
                    strArray = str.split("\\s+");
                    if("".equals(strArray[0])){
                        strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                    }
                    re = Double.parseDouble(strArray[1]);
                    return Double.valueOf(df.format(re));
                }
                i++;
            }
        } catch (Exception e) {
            log.error("获取cpu使用百分比:", e);
        } finally {
            in.close();
        }
        return re;
    }

    /**
     * 获取磁盘利用率
     *
     * @return
     * @throws Exception
     */
    public Map<String,Object> getDeskOnUse() throws Exception {
        Map<String,Object> map = new HashMap<>();
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("df");// 调用系统的“df"命令
        double all = 0;
        double use = 0;
        double re = 0;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str = null;
            String[] strArray = null;
            str = in.readLine();
            while ((str = in.readLine()) != null) {
                strArray = str.split("\\s+");
                all = all + Double.parseDouble(strArray[1]);
                use = use + Double.parseDouble(strArray[2]);

            }
            map.put("all",Double.valueOf(df.format(all)));
            map.put("use",Double.valueOf(df.format(use)));
        } catch (Exception e) {
            log.error("获取磁盘利用率:", e);
        } finally {
            in.close();
        }
        return map;
    }

}