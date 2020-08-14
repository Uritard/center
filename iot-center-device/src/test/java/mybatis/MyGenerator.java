package mybatis;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyGenerator {

//    public static final String TABLES = "sys_menu,sys_role_menu";
    public static final String TABLES = "t_std_region";

    /*public static String scanner(String someThing) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + someThing + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String sc = scanner.next();
            if (StringUtils.isNotEmpty(sc)) {
                return sc;
            }
        }
        throw new MybatisPlusException("请输入正确的" + someThing + "！");
    }*/

    public static void main(String args[]) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath2 = "F:/workspace/jiejuefangan/IotCenter/iot-center-device";
        String projectPath = System.getProperty("user.dir").replace("\\", "/");
        System.out.println("projectPath: "+projectPath);
        gc.setOutputDir(projectPath2 + "/src/main/java");
        gc.setBaseResultMap(true);// XML ResultMap
        gc.setBaseColumnList(true);// XML columList
        gc.setAuthor("tt");
        gc.setOpen(false);
        gc.setFileOverride(false);
        gc.setDateType(DateType.ONLY_DATE);//配置时间类型策略
//        gc.setIdType(ASSIGN_UUID);
        gc.setSwagger2(true);
        gc.setMapperName("%sDao");
        gc.setXmlName("%sMapper");
        gc.setServiceName("%sService");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:mysql://localhost:3306/smartelectric?serverTimezone=GMT&useUnicode=true&useSSL=false&characterEncoding=utf8");
        dataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("yjh@123!");
//        //实体类输出类型转换
//        dataSourceConfig.setTypeConvert(
//            new MySqlTypeConvert() {
//                @Override
//                public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
//                    System.out.println("转换类型：" + fieldType);
//                    //Integer转换成INTEGER
//                    if ( fieldType.toLowerCase().contains( "Integer" ) ) {
//                        return DbColumnType.INTEGER;
//                    }
//                    //将数据库中datetime转换成TIMESTAMP
//                    if ( fieldType.toLowerCase().contains( "datetime" ) ) {
//                        return DbColumnType.TIMESTAMP;
//                    }
//                    //将数据库中datetime转换成TIMESTAMP
//                    if ( fieldType.toLowerCase().contains( "String" ) ) {
//                        return DbColumnType.STRING;
//                    }
//                    return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
//                }
//            }
//        );
        mpg.setDataSource(dataSourceConfig);

        // 包配置
        PackageConfig pc = new PackageConfig();
//        pc.setModuleName(scanner("请输入你的包名"));
        pc.setModuleName("ttGenerator");
        pc.setParent("com.yjh.device.module");//你哪个父目录下创建包
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
                this.setMap(map);
                /*
                自定义属性注入: 模板配置：abc=${cfg.abc}
                 */
            }
        };

        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        // String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/java/com/yjh/platform/mapper" + pc.getModuleName()
                        + "/" + tableInfo.getEntityName() + "Dao" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        //配置 自定义模板
        TemplateConfig templateConfig = new TemplateConfig()
                .setController("templates/controller.java")//指定Entity生成使用自定义模板
                .setService("templates/service.java")
                .setEntity("templates/entity.java")
                .setMapper("templates/mapper.java")
                .setXml("templates/mapper.xml")
                .setServiceImpl(null);//不生成Impl
        mpg.setTemplate(templateConfig);

        // 策略配置,数据库表配置
        StrategyConfig strategy = new StrategyConfig();
        //数据库表映射到实体的命名策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        //数据库表字段映射到实体类的命名策略
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //自定义继承entity类，添加这一个会在生成实体类的时候继承entity
        //strategy.setSuperEntityClass("com.wy.testCodeGenerator.entity");
        //实体是否为lombok模型
        strategy.setEntityLombokModel(true);
        //生成@RestController控制器
        strategy.setRestControllerStyle(true);
        //是否继承controller
        // strategy.setSuperControllerClass("com.wy.testCodeGenerator.controller");
//        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setInclude(TABLES.split(","));
        strategy.setSuperEntityColumns("id");
        //驼峰转连字符串
        strategy.setControllerMappingHyphenStyle(true);
        //表前缀
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);

        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

}