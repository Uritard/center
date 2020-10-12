package com.yjh.accessvideo.MQ.producer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class BaseProducer {

    @Value("${rmq.host}")
	private String host;
    @Value("${rmq.port}")
	private String port;
    @Value("${rmq.name}")
	private String name;
    @Value("${rmq.password}")
	private String password;
    @Value("${rmq.vhost}")
	private String vhost;
	private static ConnectionFactory factory = null;

	public Connection getConnection() throws IOException, TimeoutException {
		if (factory == null) {
			factory = new ConnectionFactory();
			factory.setHost(host);
			factory.setUsername(name);
			factory.setPassword(password);
			factory.setPort(Integer.parseInt(port));
			factory.setVirtualHost(vhost);
		}
		Connection connection = factory.newConnection();
		return connection;
	}

}
