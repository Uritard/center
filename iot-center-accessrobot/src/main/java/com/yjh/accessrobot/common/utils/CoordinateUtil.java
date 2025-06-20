package com.yjh.accessrobot.common.utils;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CoordinateUtil: 坐标转换工具类
 *
 * @author shaobinfen
 * @date 2025/05/30
 */
@Slf4j
@Component
public class CoordinateUtil {

    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 机器人编码关联地图信息
     */
    private static Map<String, MapInfo> robotReMapData = new ConcurrentHashMap<>();

    private static final String SVG_SUFFIX = ".svg";

    @SuppressWarnings("unchecked")
    public void loadMapInfo(String robotCode) {
        try {
            TRobotInfo robotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
            Integer robotType = robotInfo.getRobotType();
            if (ObjectUtils.notEqual(robotType, 905)) return;
            String mapPath = robotInfo.getPhotePath();
            if (StringUtils.isEmpty(mapPath)) throw new BusinessException("地图存储路径为空!");
            String fileFtpPathMap = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute").get("content"));
            mapPath = mapPath.replace(Constant.FTP_IMAGE_RELATIVE, fileFtpPathMap);
            Path mapFullPath = Paths.get(mapPath);
            if (Files.notExists(mapFullPath)) throw new BusinessException("地图文件不存在!");
            MapInfo mapInfo = new MapInfo();
            if (mapPath.endsWith(SVG_SUFFIX)) {
                //示例: robotLoc="58.9196 -2.15953 -2.05774"
                String robotLoc = getAttributeValueOfRoot(mapFullPath, "robotLoc");
                mapInfo.setRobotType(robotType).setMapPath(mapPath).setProportion(Double.parseDouble(robotLoc.split(" ")[0]));
            } else {
                BufferedImage imageRead = ImageIO.read(mapFullPath.toFile());
                int height = imageRead.getHeight();
                int width = imageRead.getWidth();
                String yamlPath = FilenameUtils.removeExtension(mapPath) + ".yaml";
                Path yamlFullPath = Paths.get(yamlPath);
                if (Files.notExists(yamlFullPath)) throw new BusinessException("地图yaml文件不存在!");
                Map<String, Object> mapInfoData = readYamlInfo(yamlPath);
                if (MapUtils.isNotEmpty(mapInfoData)) {
                    mapInfo.setRobotType(robotType).setMapPath(mapPath).setYamlPath(yamlPath)
                            .setResolution(ValueUtil.object2Double(mapInfoData.get("resolution"), null))
                            .setTop(ValueUtil.object2Double(mapInfoData.get("top"), null))
                            .setBottom(ValueUtil.object2Double(mapInfoData.get("bottom"), null))
                            .setLeft(ValueUtil.object2Double(mapInfoData.get("left"), null))
                            .setRight(ValueUtil.object2Double(mapInfoData.get("right"), null))
                            .setHeight(height).setWidth(width);
                }
            }
            log.info("简易机器人: {}地图信息加载成功!", robotCode);
            robotReMapData.put(robotCode, mapInfo);
        } catch (Exception e) {
            log.error("简易机器人: {}地图信息加载失败", robotCode, e);
        }
    }

    /**
     * 读取yaml文件内容
     *
     * @param yamlPath yaml文件路径
     */
    public static Map<String, Object> readYamlInfo(String yamlPath) {
        Map<String, Object> map = new HashMap<>();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(Paths.get(yamlPath))) {
            map = yaml.load(inputStream);
        } catch (IOException e) {
            log.error("读取yaml文件失败", e);
        }
        return map;
    }

    /**
     * 根据属性名获取SVG文件根元素(<svg>标签)的属性值
     *
     * @param name 属性名
     * @return 属性值
     */
    public static String getAttributeValueOfRoot(Path svgPath, String name) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try {
            Document doc = factory.createDocument(svgPath.toUri().toString());
            Element root = doc.getDocumentElement();
            String value = root.getAttribute(name);
            if (StringUtils.isNotEmpty(value)) return value;
        } catch (IOException e) {
            log.error("获取SVG文件根元素属性值失败", e);
        }
        return "";
    }

    @Data
    @Accessors(chain = true)
    public static class MapInfo {

        /**
         * 机器人类型 905:简易机器人
         */
        private Integer robotType;

        /**
         * 地图文件存储路径(svg或者png)
         */
        private String mapPath;

        /**
         * 地图yaml文件存储路径
         */
        private String yamlPath;

        /**
         * 使用svg文件时像素坐标和真实坐标的比例值
         */
        private double proportion;

        /**
         * 地图分辨率
         */
        private double resolution;

        /**
         * 地图左边框X值
         */
        private double left;

        /**
         * 地图右边框X值
         */
        private double right;

        /**
         * 地图上边框Y值
         */
        private double top;

        /**
         * 地图下边框Y值
         */
        private double bottom;

        /**
         * 地图png文件像素宽
         */
        private int width;

        /**
         * 地图png文件像素高
         */
        private int height;
    }

    /**
     * 像素点坐标转实际坐标
     * @param robotCode 机器人编码
     * @param pixelCoordinates 像素点坐标 格式: "x,y,z,a"
     * @return 真实坐标 格式: "x,y,z"
     */
    public static String pixelToActual(String robotCode, String pixelCoordinates) {
        MapInfo mapInfo = robotReMapData.get(robotCode);
        double actualX, actualY;
        try {
            String[] coordinates = pixelCoordinates.split(",");
            if (mapInfo.getMapPath().endsWith(SVG_SUFFIX)) {
                actualX = Double.parseDouble(coordinates[0]) / mapInfo.getProportion();
                actualY = Double.parseDouble(coordinates[1]) / mapInfo.getProportion();
            } else {
                actualX = Double.parseDouble(coordinates[0]) * mapInfo.getResolution() + mapInfo.getLeft();
                actualY = mapInfo.getTop() - Double.parseDouble(coordinates[1]) * mapInfo.getResolution();
            }
        } catch (Exception e) {
            return pixelCoordinates; //任何异常统一返回
        }
        return String.join(",", formatToThreeDecimals(actualX), formatToThreeDecimals(actualY), "0");
    }

    /**
     * 真实坐标转像素点坐标
     * @param robotCode 机器人编码
     * @param actualCoordinate 真实坐标 格式: "x,y,z"
     * @return 像素点坐标 格式: "x,y,z,a"
     */
    public static String actualToPixel(String robotCode, String actualCoordinate) {
        MapInfo mapInfo = robotReMapData.get(robotCode);
        double pixelX, pixelY;
        try {
            if (Objects.isNull(mapInfo) || ObjectUtils.notEqual(mapInfo.getRobotType(), 905)) return actualCoordinate;
            String[] coordinates = actualCoordinate.split(",");
            if (mapInfo.getMapPath().endsWith(SVG_SUFFIX)) {
                pixelX = Double.parseDouble(coordinates[0]) * mapInfo.getProportion();
                pixelY = Double.parseDouble(coordinates[1]) * mapInfo.getProportion();
            } else {
                pixelX = (Double.parseDouble(coordinates[0]) - mapInfo.getLeft()) / mapInfo.getResolution();
                pixelY = (mapInfo.getTop() - Double.parseDouble(coordinates[1])) / mapInfo.getResolution();
            }
        } catch (Exception e) {
            return actualCoordinate; //任何异常统一返回
        }
        return String.join(",", formatToThreeDecimals(pixelX), formatToThreeDecimals(pixelY), "0", "0");
    }

    /**
     * 四舍五入保留三位小数(末尾去零)
     */
    public static String formatToThreeDecimals(Number number) {
        DecimalFormat df = new DecimalFormat("0.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(number);
    }
}
