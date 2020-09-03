package com.yjh.gateway.commons.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author tt
 * @ClassName:
 * @Description: 随机数方法
 * @date 2019/8/13
 */
public class RandomUtil {

    public static String getRandom(int length) {
        StringBuilder secret = new StringBuilder();
        Random random = null;
        try {
            random = SecureRandom.getInstanceStrong();
            for (int i = 0; i < length; i++) {
                String str = random.nextInt(2) % 2 == 0 ? "num" : "char";
                if ("char".equalsIgnoreCase(str)) { // 产生字母
                    int nextInt = random.nextInt(2) % 2 == 0 ? 65 : 97;
                    secret.append((char) (nextInt + random.nextInt(26)));
                } else if ("num".equalsIgnoreCase(str)) { // 产生数字
                    secret.append(String.valueOf(random.nextInt(10)));
                }
            }
        }catch(NoSuchAlgorithmException e){
        }
        return secret.toString();
    }

    /**
     * 生产随机字母
     *
     * @param length
     * @return
     */
    private static String getItemName(int length) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = null;
        StringBuffer secret = new StringBuffer();
        try {
            random = SecureRandom.getInstanceStrong();
            for (int i = 0; i < length; i++) {
                int number = random.nextInt(base.length());
                secret.append(base.charAt(number));
            }
        }catch(NoSuchAlgorithmException e){
        }
        return secret.toString();
    }
}
