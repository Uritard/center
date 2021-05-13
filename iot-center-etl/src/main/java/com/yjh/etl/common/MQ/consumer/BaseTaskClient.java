package com.yjh.etl.common.MQ.consumer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BaseTaskClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String host = "rmq.host";
    private String port = "rmq.port";
    private String name = "rmq.name";
    private String password = "rmq.password";
    private static ConnectionFactory factory = null;

    public synchronized Connection getConnection(boolean autoRecovery) throws IOException, TimeoutException {
        //连接工厂
        if (factory == null) {
            log.warn("Rabbit MQ Parameter: host({}), port({}), name({}), pass({})", host, port, name, password);
            factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(name);
            factory.setPassword(password);
            factory.setPort(Integer.parseInt(port));
        }

        factory.setAutomaticRecoveryEnabled(autoRecovery);
        //与RabbitMQ服务器建立连接
        Connection connection = factory.newConnection();
        return connection;
    }


}
