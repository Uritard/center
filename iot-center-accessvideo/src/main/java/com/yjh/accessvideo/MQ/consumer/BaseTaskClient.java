package com.yjh.accessvideo.MQ.consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BaseTaskClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${rmq.host}")
    private String host;
    @Value("${rmq.port}")
    private String port;
    @Value("${rmq.name}")
    private String name;
    @Value("${rmq.password}")
    private String password;
    private static ConnectionFactory factory = null;

    public synchronized Connection getConnection(boolean autoRecovery) throws IOException, TimeoutException {
        if (factory == null) {
            log.warn("Rabbit MQ Parameter: host({}), port({}), name({}), pass({})", host, port, name, password);
            factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(name);
            factory.setPassword(password);
            factory.setPort(Integer.parseInt(port));
        }

        factory.setAutomaticRecoveryEnabled(autoRecovery);
        Connection connection = factory.newConnection();
        return connection;
    }


}
