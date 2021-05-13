package com.yjh.etl.common.MQ.producer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class BaseProducer {

	private String host = "rmq.host";
	private String port = "rmq.port";
	private String name = "rmq.name";
	private String password = "rmq.password";
	private String vhost = "rmq.vhost";
	private static ConnectionFactory factory = null;

	public Connection getConnection() throws IOException, TimeoutException {
		//创建连接工厂,并设置连接信息
		if (factory == null) {
			factory = new ConnectionFactory();
			factory.setHost(host);
			factory.setUsername(name);
			factory.setPassword(password);
			factory.setPort(Integer.parseInt(port));
			factory.setVirtualHost(vhost);
		}
		//与RabbitMQ服务器建立连接
		Connection connection = factory.newConnection();
		return connection;
	}

}
