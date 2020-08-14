//package com.yjh.device.datasource;
//
//import com.alibaba.druid.filter.config.ConfigTools;
//import com.alibaba.druid.pool.DruidDataSource;
//import com.alibaba.druid.support.http.StatViewServlet;
//import com.alibaba.druid.support.http.WebStatFilter;
//import com.github.pagehelper.PageHelper;
//import org.apache.ibatis.plugin.Interceptor;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//
//import javax.sql.DataSource;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
///**
// * @author tt
// * @version V1.0.0
// * @Package com.yjh.device.datasource
// * @date 2019/8/14 10:52
// * @Copyright 亿嘉和
// */
//@Configuration
//@ConfigurationProperties(prefix = "spring.datasource.device")
//@MapperScan(basePackages = {"com.yjh.device.module.device.dao"}, sqlSessionTemplateRef = "baseSqlSessionTemplate")
//public class DruidDatsSourceConfig {
//    private Logger logger = LoggerFactory.getLogger(DruidDatsSourceConfig.class);
//    private String type;
//    private String url;
//    private String driverClassName;
//    private String username;
//    private String password;
//    private Integer initialSize;
//    private Integer minIdle;
//    private Integer maxActive;
//    private Integer maxWait;
//    private Integer timeBetweenEvictionRunsMillis;
//    private Integer minEvictableIdleTimeMillis;
//    private String validationQuery;
//    private Boolean testWhileIdle;
//    private Boolean testOnBorrow;
//    private Boolean testOnReturn;
//    private Boolean poolPreparedStatements;
//    private Integer maxPoolPreparedStatementPerConnectionSize;
//    private String filters;
//    private String connectionProperties;
//    private String publicKey;
//
//    public String getDriverClassName() {
//        return driverClassName;
//    }
//
//    public void setDriverClassName(String driverClassName) {
//        this.driverClassName = driverClassName;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public Integer getInitialSize() {
//        return initialSize;
//    }
//
//    public void setInitialSize(Integer initialSize) {
//        this.initialSize = initialSize;
//    }
//
//    public Integer getMinIdle() {
//        return minIdle;
//    }
//
//    public void setMinIdle(Integer minIdle) {
//        this.minIdle = minIdle;
//    }
//
//    public Integer getMaxActive() {
//        return maxActive;
//    }
//
//    public void setMaxActive(Integer maxActive) {
//        this.maxActive = maxActive;
//    }
//
//    public Integer getMaxWait() {
//        return maxWait;
//    }
//
//    public void setMaxWait(Integer maxWait) {
//        this.maxWait = maxWait;
//    }
//
//    public Integer getTimeBetweenEvictionRunsMillis() {
//        return timeBetweenEvictionRunsMillis;
//    }
//
//    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
//        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
//    }
//
//    public Integer getMinEvictableIdleTimeMillis() {
//        return minEvictableIdleTimeMillis;
//    }
//
//    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
//        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
//    }
//
//    public String getValidationQuery() {
//        return validationQuery;
//    }
//
//    public void setValidationQuery(String validationQuery) {
//        this.validationQuery = validationQuery;
//    }
//
//    public Boolean getTestWhileIdle() {
//        return testWhileIdle;
//    }
//
//    public void setTestWhileIdle(Boolean testWhileIdle) {
//        this.testWhileIdle = testWhileIdle;
//    }
//
//    public Boolean getTestOnBorrow() {
//        return testOnBorrow;
//    }
//
//    public void setTestOnBorrow(Boolean testOnBorrow) {
//        this.testOnBorrow = testOnBorrow;
//    }
//
//    public Boolean getTestOnReturn() {
//        return testOnReturn;
//    }
//
//    public void setTestOnReturn(Boolean testOnReturn) {
//        this.testOnReturn = testOnReturn;
//    }
//
//    public Boolean getPoolPreparedStatements() {
//        return poolPreparedStatements;
//    }
//
//    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
//        this.poolPreparedStatements = poolPreparedStatements;
//    }
//
//    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
//        return maxPoolPreparedStatementPerConnectionSize;
//    }
//
//    public void setMaxPoolPreparedStatementPerConnectionSize(
//            Integer maxPoolPreparedStatementPerConnectionSize) {
//        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
//    }
//
//    public String getFilters() {
//        return filters;
//    }
//
//    public void setFilters(String filters) {
//        this.filters = filters;
//    }
//
//    public String getConnectionProperties() {
//        return connectionProperties;
//    }
//
//    public void setConnectionProperties(String connectionProperties) {
//        this.connectionProperties = connectionProperties;
//    }
//
//    public String getPublicKey() {
//        return publicKey;
//    }
//
//    public void setPublicKey(String publicKey) {
//        this.publicKey = publicKey;
//    }
///*
//    @Bean
//    public ServletRegistrationBean druidServlet() {
//        ServletRegistrationBean reg = new ServletRegistrationBean();
//        reg.setServlet(new StatViewServlet());
//        reg.addUrlMappings("/druid/*");
//        reg.addInitParameter("loginUsername", username);
//        reg.addInitParameter("loginPassword", password);
//        return reg;
//    }*/
//
///*
//    @Bean
//    public FilterRegistrationBean filterRegistrationBean() {
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//        filterRegistrationBean.setFilter(new WebStatFilter());
//        filterRegistrationBean.addUrlPatterns("/*");
//        filterRegistrationBean.addInitParameter("exclusions",
//                "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
//        filterRegistrationBean.addInitParameter("profileEnable", "true");
//        return filterRegistrationBean;
//    }
//*/
//
//    @Bean
//    @Primary
//    public DataSource druidDataSource() throws Exception {
//        DruidDataSource datasource = new DruidDataSource();
//        datasource.setUrl(url);
//        datasource.setUsername(username);
//        datasource.setPassword(ConfigTools.decrypt(publicKey, password));
//        datasource.setDriverClassName(driverClassName);
//        datasource.setInitialSize(initialSize);
//        datasource.setMinIdle(minIdle);
//        datasource.setMaxActive(maxActive);
//        datasource.setMaxWait(maxWait);
//        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
//        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
//        datasource.setValidationQuery(validationQuery);
//        datasource.setTestWhileIdle(testWhileIdle);
//        datasource.setTestOnBorrow(testOnBorrow);
//        datasource.setTestOnReturn(testOnReturn);
//        String paramArray[] = connectionProperties.split(";");
//        datasource.setConnectionProperties(connectionProperties);
//        try {
//            datasource.setFilters(filters);
//        } catch (SQLException e) {
//            logger.error("========druid configuration initialization filter========", e);
//        }
//        return datasource;
//    }
//
//    /**
//     * 配置Druid的监控
//     */
//
//    @Bean
//    public ServletRegistrationBean statViewServlet() {
//        ServletRegistrationBean<StatViewServlet> bean =
//                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
//        Map<String, String> initParms = new HashMap<>(3);
//        initParms.put("loginUsername", "root");
//        initParms.put("loginPassword", "yjh@123!");
//        //允许访问,默认所有的
//        initParms.put("allow", "");
//        bean.setInitParameters(initParms);
//        return bean;
//    }
//
//    /**
//     * 配置一个Web监控的Filter
//     */
//    @Bean
//    public FilterRegistrationBean webStatFilter() {
//        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
//        Map<String, String> initParams = new HashMap<>(2);
//        initParams.put("exclusions", "*.js,*.css,/druid/*");
//        //拦截所以请求
//        bean.setUrlPatterns(Arrays.asList("/*"));
//        bean.setInitParameters(initParams);
//        return bean;
//    }
//
//    @Bean(name = "baseTransactionManager")
//    public DataSourceTransactionManager setTransactionManager() throws Exception{
//        return new DataSourceTransactionManager(druidDataSource());
//    }
//
//    @Bean(name = "baseSqlSessionFactory")
//    public SqlSessionFactory setSqlSessionFactory() throws Exception {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setDataSource(druidDataSource());
//        //mybatis 下划线转驼峰
//        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
//        configuration.setMapUnderscoreToCamelCase(true);
//        bean.setConfiguration(configuration);
//        //mybatis 分页插件
//        PageHelper pageHelper = new PageHelper();
//        Properties p = new Properties();
//        p.setProperty("offsetAsPageNum", "true");
//        p.setProperty("rowBoundsWithCount", "true");
//        p.setProperty("reasonable", "true");
//        //通过设置pageSize=0或者RowBounds.limit = 0就会查询出全部的结果。
//        //p.setProperty("pageSizeZero", "true");
//        //配置mysql数据库的方言
//        //p.setProperty("dialect","mysql");
//        pageHelper.setProperties(p);
//        //添加插件
//        bean.setPlugins(new Interceptor[]{pageHelper});
//
//
//        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/mapper/*/*.xml"));
//        return bean.getObject();
//    }
//
//    @Bean(name = "baseSqlSessionTemplate")
//    public SqlSessionTemplate setSqlSessionTemplate(@Qualifier("baseSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}
