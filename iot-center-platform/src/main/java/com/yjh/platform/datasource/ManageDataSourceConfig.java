package com.yjh.platform.datasource;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author tt
 * @Description: 数据库连接
 * @date 2020/6/29
 */
@Configuration
@MapperScan(basePackages = {"com.yjh.platform.module.*.dao"}, sqlSessionTemplateRef = "baseSqlSessionTemplate")
public class ManageDataSourceConfig {

    private Logger log = LoggerFactory.getLogger(ManageDataSourceConfig.class);

    private static String publicKey;

    private static String username;

    private static String password;

    private static String database;


    @Bean(name = "baseDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.platform")
    public DataSource setDataSource() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();
        List filterList=new ArrayList<>();
        filterList.add(wallFilter());
        druidDataSource.setProxyFilters(filterList);
        log.info("druidDataSource001: "+druidDataSource);
//        druidDataSource.setPassword(ConfigTools.decrypt(publicKey, password));
        return druidDataSource;
    }

    /**
     * 配置Druid的监控
     */

    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParms = new HashMap<>(3);
        initParms.put("loginUsername", "root");
        initParms.put("loginPassword", "yjh@123!");
        //允许访问,默认所有的
        initParms.put("allow", "");
        bean.setInitParameters(initParms);
        return bean;
    }

    /**
     * 配置一个Web监控的Filter
     */
    @Bean
    public FilterRegistrationBean webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
        Map<String, String> initParams = new HashMap<>(2);
        initParams.put("exclusions", "*.js,*.css,/druid/*");
        //拦截所以请求
        bean.setUrlPatterns(Arrays.asList("/*"));
        bean.setInitParameters(initParams);
        return bean;
    }

    @Bean(name = "baseTransactionManager")
    public DataSourceTransactionManager setTransactionManager(@Qualifier("baseDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "baseSqlSessionFactory")
    public SqlSessionFactory setSqlSessionFactory(@Qualifier("baseDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //mybatis 下划线转驼峰
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        //mybatis 分页插件
        PageInterceptor pageHelper = new PageInterceptor();
        Properties p = new Properties();
        p.setProperty("offsetAsPageNum", "true");
        p.setProperty("rowBoundsWithCount", "true");
        p.setProperty("reasonable", "false");
        //通过设置pageSize=0或者RowBounds.limit = 0就会查询出全部的结果。
        //p.setProperty("pageSizeZero", "true");
        //配置mysql数据库的方言
        //p.setProperty("dialect","mysql");
        pageHelper.setProperties(p);
        //添加插件
        bean.setPlugins(new Interceptor[]{pageHelper});


        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "mybatisPlusSqlSessionFactory")
    public SqlSessionFactory mybatisPlusSqlSessionFactory(@Qualifier("baseDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //mybatis 下划线转驼峰
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        //mybatis 分页插件
        PageInterceptor pageHelper = new PageInterceptor();
        Properties p = new Properties();
        p.setProperty("offsetAsPageNum", "true");
        p.setProperty("rowBoundsWithCount", "true");
        p.setProperty("reasonable", "false");
        //通过设置pageSize=0或者RowBounds.limit = 0就会查询出全部的结果。
        //p.setProperty("pageSizeZero", "true");
        //配置mysql数据库的方言
        //p.setProperty("dialect","mysql");
        pageHelper.setProperties(p);
        //添加插件

        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        bean.setPlugins(pageHelper, mybatisPlusInterceptor);


        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "baseSqlSessionTemplate")
    public SqlSessionTemplate setSqlSessionTemplate(@Qualifier("mybatisPlusSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public WallFilter wallFilter(){
        WallFilter wallFilter=new WallFilter();
        wallFilter.setConfig(wallConfig());
        return wallFilter;

    }

    @Bean
    public WallConfig wallConfig(){
        WallConfig config =new WallConfig();
        config.setMultiStatementAllow(true);//允许一次执行多条语句
        config.setNoneBaseStatementAllow(true);//允许非基本语句的其他语句
        return config;

    }

//    public static void main(String[] args) {
//        try {
//            String s = ConfigTools.decrypt("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIOM2x5chWV82jFM7B4gcHn5HmaCR3o9aMQhWImCNgHTJ/1jvwb4tTyFML0dfZ9xVIYB6+aPiCM5rCwEJIl4KIUCAwEAAQ==",
//                    "e4Z7dhOBrSO7qhzLrg33UiE5O38K6IwQ9NoBcnAupG3+E++kV7XNKLwuoryh9NF/xAyEV77xrzPD5Y5W/6c+Rw==");
//            System.out.println(s);
//        } catch (Exception e) {
//            e.getMessage();
//        }
//    }

    public static String getPublicKey() {
        return publicKey;
    }

    @Value("${spring.datasource.platform.publicKey}")
    public void setPublicKey(String publicKey) {
        ManageDataSourceConfig.publicKey = publicKey;
    }

    public static String getUsername() {
        return username;
    }

    @Value("${spring.datasource.platform.username}")
    public void setUsername(String username) {
        ManageDataSourceConfig.username = username;
    }

    public static String getPassword() {
        return password;
    }

    @Value("${spring.datasource.platform.password}")
    public void setPassword(String password) {
        try {
            ManageDataSourceConfig.password = ConfigTools.decrypt(publicKey, password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String getDatabase() {
        return database;
    }

    @Value("${spring.datasource.platform.url}")
    public void setDatabase(String url) {
        String pre = StringUtils.substringBefore(url, "?");
        String db = StringUtils.substringAfterLast(pre, "/");
        ManageDataSourceConfig.database = db;
    }
}
