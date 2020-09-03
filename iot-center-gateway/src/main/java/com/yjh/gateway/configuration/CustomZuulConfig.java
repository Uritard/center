package com.yjh.gateway.configuration;

import com.yjh.gateway.module.gateway.dao.RouteDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tt on 2019/6/3.
 */
@Configuration
public class CustomZuulConfig {
    @Autowired
    ZuulProperties zuulProperties;
    @Autowired
    ServerProperties server;
    @Autowired
    RouteDao routeDao;

    @Bean
    public CustomRouteLocator routeLocator() {
        CustomRouteLocator routeLocator = new CustomRouteLocator(server.getServlet().getServletPrefix(), this.zuulProperties);
        routeLocator.setRouteDao(routeDao);
        return routeLocator;
    }
}
