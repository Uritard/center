package com.yjh.platform.common.utils;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/10/20 - 15:46
 */
public class EasyPoiUtil {
        /*
        导出excel
        * */
        public static void exportExcel(List<?> list,  String sheetName, String fileName,
                                       boolean isNeedSignature ) {
            TemplateExportParams params = new TemplateExportParams();
            params.setSheetName(sheetName);// 设置sheetName
            params.setTemplateUrl("D:/MyDocuments/xls文档/reportTemplate.xlsx");
    //        System.out.println("<------------params------------->:"+params);
            //获取报表内容
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            Workbook workbook = ExcelExportUtil.exportExcel(params, data);
            try {
//                File file = new File("D:/MyDocuments/workspace_idea/IotCenterDev/IotCenter/iot-center-logs/"+fileName+".xlsx");
//                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream("D:/MyDocuments/workspace_idea/IotCenterDev/templateFile/"+fileName+".xlsx");
                workbook.write(fileOutputStream);
                System.out.println("<----------导出Excel成功---------->");
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("---------->导出Excel异常---------->");
            }
            //        Runtime.getRuntime().exec(transUrl);

        }
        /*
        获取图片转换为字节流
         */
        public static byte[] getImageFromNetByUrl(String strUrl) {
            if (!isURL(strUrl)){
                return null;
            }
            try {
                URL url = new URL(strUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2 * 1000);
                InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
                byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
                return btImg;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        /*从输入流中获取字节流数据*/
        public static byte[] readInputStream(InputStream inStream) throws Exception {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[10240];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        }

        public static boolean isURL(String str) {
            str = str.toLowerCase();
            String regex = "^((https|http|ftp|rtsp|mms)?://)"
                    + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
                    + "(([0-9]{1,3}\\.){3}[0-9]{1,3}"
                    + "|"
                    + "([0-9a-z_!~*'()-]+\\.)*"
                    + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\."
                    + "[a-z]{2,6})"
                    + "(:[0-9]{1,5})?"
                    + "((/?)|"
                    + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
            return str.matches(regex);
        }

    }

