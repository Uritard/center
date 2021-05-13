package com.yjh.etl.common.MQ.producer;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class TaskProducer {

	@Autowired
	private BaseProducer producer;

	private Logger log = LoggerFactory.getLogger(TaskProducer.class);

	private String robotInfo = "rmq.taskquename.robotInfo";
	private String robotState = "rmq.taskquename.robotState";
	private String taskData = "rmq.taskquename.taskData";
	private String taskResult = "rmq.taskquename.taskResult";
	private String hjData = "rmq.taskquename.hjData";
	private String hwData = "rmq.taskquename.hwData";
	private String gdsbAlert = "rmq.taskquename.gdsbAlert";
	private String rmqType = "rmq.type3";

    private static final String route = "rmq.route";

	  /**
	   * 任务消息入队
	 * @throws TimeoutExceptionselectHistory
	 * @throws IOException
	   */
	  public String produceTask(JSONObject jobj, String routeKey) throws IOException, TimeoutException{
		  Connection connection = null;
		  Channel channel = null;
		  String message = jobj.toJSONString();
		 try {
			connection = producer.getConnection();
			channel = connection.createChannel();//建立信道
			log.info("TaskProducer 任务上传 消息入队  "+message);
	        if ("robotinfo".equals(routeKey)) {
	        	/*
	        	*声明队列,会在RabbitMQ中创建一个队列.如果已经创建过,就不能再使用其他参数来创建
	        	*
				*参数含义：
				*	队列名称
				*	队列持久化,true表示RabbitMQ重启后队列仍存在
				*	排他独有,true表示限制仅当前连接可用
				*	当最后一个消费者断开后,是否删除队列
				* 	其他参数
				* */
				//channel.queueDeclare(robotInfo, durable, false, false, null);//队列持久化
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
				/*
				* 发布消息,把消息向默认交换机发送,默认交换机隐含与所有队列绑定,routing key即为队列名称（第二个参数）
				*
				* 参数含义：
				* 	交换机名称
				* 	对于默认交换机,路由键就是目标队列名称
				* 	其他参数,例如头信息
				* 	消息内容byte[]数组
				* */
	        	channel.basicPublish(route, robotInfo, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());//消息持久化
	        }
	        if ("robotstate".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, robotState, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("taskdata".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, taskData, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("taskresult".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, taskResult, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("hjdata".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, hjData, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("hwdata".equals(routeKey)) {
				//channel.exchangeDeclare(route ,robotInfo, true, false, false, null);
	        	channel.basicPublish(route, hwData, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("gdsbalert".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, gdsbAlert, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        }
	        log.info("TaskProducer 任务上传 消息入队成功 "+routeKey);
		 } catch (Exception e) {
             log.info("TaskProducer 任务上传 消息入队错误 "+routeKey, e);
		 }finally {
		 	if (channel != null) { channel.close(); }
			if (connection != null) { connection.close(); }
		}
		return message;
	  }

	  /**
	   * 任务消息入队
	   * @param
	   * @param
	 * @throws TimeoutException
	 * @throws IOException
	   */
	  public void produceTest(String CODE,String THREADNUM,String FILENAME) throws IOException, TimeoutException{
		  Connection connection = null;
		  Channel channel = null;
		  String EXCHANGE_NAME = "UPLOAD_TEST_EXCHANGE";
		 try {
			connection = producer.getConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
	        boolean durable = false;
	        JSONObject jobj = new JSONObject();
	        jobj.put("CODE", CODE);
	        jobj.put("THREADNUM", THREADNUM);
	        jobj.put("FILENAME", FILENAME);
	        channel.queueDeclare(EXCHANGE_NAME, durable, false, false, null);
	        String message = jobj.toJSONString();
	        log.info("produceTest 任务上传 消息入队  "+message);
	        channel.basicPublish(EXCHANGE_NAME,"" , null, message.getBytes());
	        log.info("produceTest 任务上传 消息入队成功 "+robotInfo);
		 } catch (Exception e) {
		    log.info("produceTest 任务上传 消息入队错误 "+robotInfo, e);
		 }finally {
		 	if (channel != null) {
				channel.close();
			}

			if (connection != null) {
				connection.close();
			}
		}
	  }

//	    public static void main(String[] argv) throws java.io.IOException, Exception {
//
//	        ConnectionFactory factory = new ConnectionFactory();
//	        factory.setHost("192.168.2.60");  
//			factory.setUsername("a200");  
//			factory.setPassword("a200"); 
//			factory.setPort(5672);  
//			
//	        Connection connection = factory.newConnection();
//	        Channel channel = connection.createChannel();
//	        boolean durable = true;
//	        
//	        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
//	        for(int i = 0 ; i < 5; i++){
//	            String message = "Hello World! " + i;
//	            channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
//	            System.out.println(" [x] Sent '" + message + "'");
//	        }
//	        channel.close();
//	        connection.close();
//	    }

	/*private static TaskProducer instance = null;

    public synchronized static TaskProducer getInstance() {
        if (null == instance) { instance = new TaskProducer(); }
        return instance;
    }*/


}
