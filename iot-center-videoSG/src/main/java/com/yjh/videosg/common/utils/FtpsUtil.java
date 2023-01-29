package com.yjh.videosg.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FtpsUtil {

    /**
     * 本地字符编码
     */
    private static String LOCAL_CHARSET = "UTF-8";


    private static String key_path = "D://ftp//apache-ftpserver-1.1.1//res//ftpserver.jks";

    // private static String host = "192.168.100.9";
    // private static int port = 10012;
    // private static String unConstans = "jysp";
    // private static String pwConstans = "jydw";


    // FTP协议里面，规定文件名编码为iso-8859-1
    private static String SERVER_CHARSET = "ISO-8859-1";

    private static String key_pw = "1";

    private static KeyManager getKeyManager() throws Exception {
        KeyStore key_ks = KeyStore.getInstance("JKS");
        key_ks.load(new FileInputStream(key_path), key_pw.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(key_ks, key_pw.toCharArray());
        KeyManager[] km = kmf.getKeyManagers();
        System.out.println("km len: " + km.length);
        return km[0];
    }


    private static TrustManager getTrustManager() throws Exception {
        KeyStore trust_ks = KeyStore.getInstance("JKS");
        trust_ks.load(new FileInputStream(key_path), key_pw.toCharArray());
        TrustManagerFactory tf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tf.init(trust_ks);
        TrustManager[] tm = tf.getTrustManagers();
        System.out.println("tm len: " + tm.length);
        return tm[0];
    }

/*
    public static void putFileForList(List<byte[]> file,
                                      List<String> remoteFilename) throws NoSuchAlgorithmException {
        try {
            log.info("-------------------------------文件上传开始");
            FTPSClient ftpClient = new FTPSClient(true);
//            ftpClient.setTrustManager(getTrustManager());
//            ftpClient.setKeyManager(getKeyManager());
            ftpClient.connect(host, port);
            // Connect to host
            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {

                // Login
                if (ftpClient.login(unConstans, pwConstans)) {
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                            "OPTS UTF8", "ON"))) {
                        // 开启服务器对UTF-8的支持，
                        LOCAL_CHARSET = "UTF-8";
                    }

                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    ftpClient.setControlEncoding(LOCAL_CHARSET);
                    // Set protection buffer size
                    ftpClient.execPBSZ(0);
                    // Set data channel protection to private
                    ftpClient.execPROT("P");
                    // Enter local passive mode
                    ftpClient.enterLocalPassiveMode();

                    // Store file on host
                    for (int i = 0; i < file.size(); i++) {
                        String fileName = remoteFilename.get(i);
                        // 上传文件名编码转换
                        fileName = new String(remoteFilename.get(i).getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                        InputStream is = new ByteArrayInputStream(file.get(i));
                        String[] dirs = fileName.split("/");
                        for (String dir : dirs) {
                            ftpClient.mkd(dir);
                            ftpClient.changeWorkingDirectory(dir);
                        }

                        if (ftpClient.storeFile(fileName, is)) {
                            log.info(unConstans + "," + remoteFilename.get(i));
                        } else {
                            log.info("Could not store file");
                        }
                        is.close();
                    }
                    // Logout
                    ftpClient.logout();
                } else {
                    System.out.println("FTP login failed");
                }

                // Disconnect
                ftpClient.disconnect();

            } else {
                System.out.println("FTP connect to host failed");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("FTP client received network error");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public static void putFile(String filepath,
                               String remoteFilename,String host,int port,String key_pw,String username,String password) throws NoSuchAlgorithmException {
        try {
            log.info("-------------------------------文件往{}:{}上传开始", host, port);
            log.info("filepath:{},remoteFilename:{},host:{},port:{},key_pw:{},username:{},password:{}",
                    filepath,remoteFilename,host,port,key_pw,username,password);
            File file = new File(filepath);
            try {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                byte[] b = new byte[1000];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                fis.close();
                byte[] data = bos.toByteArray();
                bos.close();

                FTPSClient ftpClient = null;
                if (key_pw.equals("1") || key_pw.equals("2")) {
                    ftpClient = new FTPSClient("TLS",true);
                }else {
                    ftpClient = new FTPSClient();
                }
                ftpClient.setAuthValue("TLS");
                //        ftpClient.setTrustManager(getTrustManager());
                //      ftpClient.setKeyManager(getKeyManager());
                ftpClient.connect(host, port);
                // Connect to host
                int reply = ftpClient.getReplyCode();

                SSLContext sslContext = SSLContext.getInstance("SSL");

                if (FTPReply.isPositiveCompletion(reply)) {

                    // Login
                    if (ftpClient.login(username, password)) {
                        if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                                "OPTS UTF8", "ON"))) {
                            // 开启服务器对UTF-8的支持，
                            LOCAL_CHARSET = "UTF-8";
                        }

                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                        ftpClient.setControlEncoding(LOCAL_CHARSET);
                        if(key_pw.equals("1")){
                            // Set protection buffer size
                            ftpClient.execPBSZ(0);
                            // Set data channel protection to private
                            ftpClient.execPROT("P");
                            // Enter local passive mode
                            ftpClient.enterLocalPassiveMode();
                        }else if(key_pw.equals("2")){
                            // Set protection buffer size
                            ftpClient.execPBSZ(0);
                            // Set data channel protection to private
                            ftpClient.execPROT("P");
                            ftpClient.enterLocalActiveMode();
                        }
                        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                        // Store file on host
                        String fileName = remoteFilename;
                        // 上传文件名编码转换
                        fileName = new String(remoteFilename.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                        InputStream is = new ByteArrayInputStream(data);
                        String[] dirs = fileName.split("/");
                        String fianlName = dirs[dirs.length - 1];
                        for (int i = 0; i < dirs.length - 1; i++) {
                            ftpClient.mkd(dirs[i]);
                            ftpClient.changeWorkingDirectory(dirs[i]);
                        }
//                        ftpClient.enterLocalPassiveMode();
                        if (ftpClient.storeFile(fianlName, is)) {
                            log.info(username + "," + remoteFilename);
                        } else {
                            log.info("code :"+ftpClient.getReplyCode());
                            log.info("message :"+ftpClient.getReplyString());
                            log.info("Could not store file");
                        }
                        is.close();

                        // Logout
                        ftpClient.logout();
                    } else {
                        log.info("FTP login failed---登录失败");
                    }

                    // Disconnect
                    ftpClient.disconnect();
                } else {
                    log.info("FTP connect to host failed---连接失败");
                }
            } catch (IOException ioe) {
                log.error("FTPS上传文件失败"+ioe);
            } catch (Exception e) {
                log.error("FTPS上传文件错误"+e);
            }
        } catch (Exception e) {
            log.error("FTPS上传文件参数有误"+e);
        }
    }

/*
    public static void putFileTest(String filepath,
                               String remoteFilename) throws NoSuchAlgorithmException {
        try {
            log.info("-------------------------------文件上传开始");
            host = "192.168.9.226";
            port = 10012;
            unConstans = "admin";
            pwConstans = "1";
            File file = new File(filepath);
            try {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
                byte[] b = new byte[1000];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                fis.close();
                byte[] data = bos.toByteArray();
                bos.close();

                FTPSClient ftpClient = new FTPSClient("TLS",true);
                ftpClient.setAuthValue("TLS");
                //        ftpClient.setTrustManager(getTrustManager());
                //      ftpClient.setKeyManager(getKeyManager());
                ftpClient.connect(host, port);
                // Connect to host
                int reply = ftpClient.getReplyCode();

                SSLContext sslContext = SSLContext.getInstance("SSL");

                if (FTPReply.isPositiveCompletion(reply)) {

                    // Login
                    if (ftpClient.login(unConstans, pwConstans)) {
                        if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                                "OPTS UTF8", "ON"))) {
                            // 开启服务器对UTF-8的支持，
                            LOCAL_CHARSET = "UTF-8";
                        }

                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                        ftpClient.setControlEncoding(LOCAL_CHARSET);
                            // Set protection buffer size
                            ftpClient.execPBSZ(0);
                            // Set data channel protection to private
                            ftpClient.execPROT("P");
                            // Enter local passive mode
                            ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
                        // Store file on host
                        String fileName = remoteFilename;
                        // 上传文件名编码转换
                        fileName = new String(remoteFilename.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                        InputStream is = new ByteArrayInputStream(data);
                        String[] dirs = fileName.split("/");
                        String fianlName = dirs[dirs.length - 1];
                        for (int i = 0; i < dirs.length - 1; i++) {
                            ftpClient.mkd(dirs[i]);
                            ftpClient.changeWorkingDirectory(dirs[i]);
                        }
//                        ftpClient.enterLocalPassiveMode();
                        if (ftpClient.storeFile(fianlName, is)) {
                            log.info(unConstans + "," + remoteFilename);
                        } else {
                            log.info("code :"+ftpClient.getReplyCode());
                            log.info("message :"+ftpClient.getReplyString());
                            log.info("Could not store file");
                        }
                        is.close();

                        // Logout
                        ftpClient.logout();
                    } else {
                        log.info("FTP login failed---登录失败");
                    }

                    // Disconnect
                    ftpClient.disconnect();
                } else {
                    log.info("FTP connect to host failed---连接失败");
                }
            } catch (IOException ioe) {
                log.error("FTPS上传文件失败"+ioe);
            } catch (Exception e) {
                log.error("FTPS上传文件错误"+e);
            }
        } catch (Exception e) {
            log.error("FTPS上传文件参数有误"+e);
        }
    }
*/

    public static boolean isFTPFileExist(String filePath,String host,int port,String key_pw,String username,String password) {
        try {
            FTPSClient ftpClient = null;
            if (key_pw.equals("1") || key_pw.equals("2")) {
                ftpClient = new FTPSClient("TLS", true);
            } else {
                ftpClient = new FTPSClient();
            }

            ftpClient.setAuthValue("TLS");
            ftpClient.connect(host, port);
            // Connect to host
            int reply = ftpClient.getReplyCode();
            SSLContext sslContext = SSLContext.getInstance("SSL");

            if (FTPReply.isPositiveCompletion(reply)) {

                // Login
                if (ftpClient.login(username, password)) {
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                            "OPTS UTF8", "ON"))) {
                        // 开启服务器对UTF-8的支持，
                        LOCAL_CHARSET = "UTF-8";
                    }

                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    ftpClient.setControlEncoding(LOCAL_CHARSET);
                    if (key_pw.equals("1")) {
                        // Set protection buffer size
                        ftpClient.execPBSZ(0);
                        // Set data channel protection to private
                        ftpClient.execPROT("P");
                        // Enter local passive mode
                        ftpClient.enterLocalPassiveMode();
                    } else if (key_pw.equals("2")) {
                        // Set protection buffer size
                        ftpClient.execPBSZ(0);
                        // Set data channel protection to private
                        ftpClient.execPROT("P");
                        ftpClient.enterLocalActiveMode();
                    }
                    ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

                    // 提取绝对地址的目录以及文件名
                    filePath = filePath.replace("ftp://"+host+":"+port+"/", "");
                    String file = filePath.substring(filePath.lastIndexOf("/")+1);
                    FTPFile[] list = ftpClient.listFiles(filePath);
                    for (FTPFile ftpFile : list) {
                        if (ftpFile.getName().equals(file)) {
                            return true;
                        }
                    }

                    // Logout
                    ftpClient.logout();
                } else {
                    log.info("FTP login failed---登录失败");
                }

                // Disconnect
                ftpClient.disconnect();
            } else {
                log.info("FTP connect to host failed---连接失败");
            }
        } catch (Exception e) {
            log.error("FTPS上传文件参数有误" + e);
        }
        return false;
    }


        public static void main(String[] args) throws NoSuchAlgorithmException {
        String path = "D://test/test.txt";
        String fileName = "/3/E200/test.txt";
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            List<byte[]> list1 = new ArrayList<byte[]>();
            List<String> list2 = new ArrayList<String>();
            list1.add(data);
            list2.add(fileName);
            // FtpsUtil.putFileForList(list1, list2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
