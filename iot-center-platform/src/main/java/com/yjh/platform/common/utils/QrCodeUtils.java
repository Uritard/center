package com.yjh.platform.common.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;

@Slf4j
public class QrCodeUtils {

    /**
     * 识别二维码内容信息
     * @return 二维码内容
     * @throws NotFoundException NotFoundException
     * @date 2019/9/10 1:59
     */
    public String decodeQrCode(String filePath) throws NotFoundException, IOException {

        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            boolean result = file.getParentFile().mkdirs();
            log.info(" create directory {} {}", file.getParent(), result);
        }
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) return null;
        String data = decodeQrCodeImage(image);
        log.info(" Qr code from [{}] data is -> {}", file.getAbsolutePath(), data);
        return data;
    }

    /**
     * 识别二维码内容信息
     *
     * @param image 二维码图片信息BufferedImage
     * @return 二维码内容
     * @date 2019/9/10 1:59
     */
    private static String decodeQrCodeImage(BufferedImage image) throws NotFoundException {
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        HashMap<DecodeHintType, Object> hints = new HashMap<>(4);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }

}
