package com.yjh.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: lqh
 * @Date: 2022/05/25
 */
@Slf4j
public class FileUtil {

    private static List<String> nameList = new ArrayList<>();

    static{
        nameList.add("ZIP");
        nameList.add("XLS");
        nameList.add("XLSX");
        nameList.add("XLSB");
        nameList.add("XLSM");
        nameList.add("XLST");
        nameList.add("XML");
    }

    public static boolean checkFileName(String fileName){
        String name = StringUtils.substringAfterLast(fileName,".").toUpperCase();
        if (nameList.contains(name)){
            return true;
        }
        log.error("上传文件不在规定文件范围内: {} - {}", fileName, name);
        return false;
    }

    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static boolean copyFileUsingStream(String sourcePath, String descPath) {
        File source = new File(sourcePath);
        File dest = new File(descPath);

        try {
            FileUtils.copyFile(source, dest);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * @param fileList 压缩多个文件夹名称 全路径
     * @param zipFileName 压缩文件名称
     * @param basePath 压缩文件的根目录
     * @param path 压缩到那个路径下
     *                    此方法保留空白文件
     */
    public static void zip(List<String> fileList, String zipFileName,String basePath,String path)throws Exception{
        if(fileList ==null || fileList.size() ==0){
            return;
        }
        File inputFile = null;
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path+File.separator+zipFileName));;
        for (String str: fileList) {
            inputFile = new File(str);
            if (!inputFile.exists()){
                log.info("文件：{} 不存在",str);
                continue;
            }

            Boolean flag = true;
            if(!str.contains("/")){
                flag = false;
            }
            zip(out, inputFile, basePath+"/"+inputFile.getName(),flag);
        }
        out.flush();
        out.close();
    }
    public static void zip(ZipOutputStream out, File f, String base,Boolean flag) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
//			out.putNextEntry(new org.apache.tools.zip.ZipEntry(base + "/"));
//			base = base.length() == 0 ? "" : base + "/";
            if(base.length() == 0){
                base = "";
            }else{
                out.putNextEntry(new ZipEntry(base + "/"));
                base = base + "/";
            }
            if(null!=fl && flag){
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + fl[i].getName(),true);
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(base));
            FileInputStream in = new FileInputStream(f);
            int b;
            log.info(base);
//			while ((b = in.read()) != -1) {
//				out.write(b);
//			}
            byte[] buf = new byte[1024*10];//每秒100k
            while((b = in.read(buf))!=-1){//循环读取
                out.write(buf, 0, b);
            }
            if (in != null) {
                in.close();
            }
        }
    }
}
