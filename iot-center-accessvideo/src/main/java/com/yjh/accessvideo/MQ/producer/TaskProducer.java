package com.yjh.accessvideo.MQ.producer;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class TaskProducer {

	@Autowired
	private BaseProducer producer;

    private Logger log = LoggerFactory.getLogger(TaskProducer.class);

    @Value("${rmq.taskquename.robotInfo}")
	private String robotInfo;
    @Value("${rmq.taskquename.robotState}")
	private String robotState;
    @Value("${rmq.taskquename.taskData}")
	private String taskData;
    @Value("${rmq.taskquename.taskResult}")
	private String taskResult;
    @Value("${rmq.type3}")
	private String rmqType;

    @Value("${rmq.route}")
    private static String route;

	  /**
	   * 任务消息入队
	 * @throws TimeoutException
	 * @throws IOException
	   */
	  public String produceTask(JSONObject jobj, String routeKey) throws IOException, TimeoutException{
		  Connection connection = null;
		  Channel channel = null;
		  String message = jobj.toJSONString();
		 try {
			connection = producer.getConnection();
			channel = connection.createChannel();
             log.info("TaskProducer 任务上传 消息入队  "+message);
	        if ("robotinfo".equals(routeKey)) {
				//channel.queueDeclare(robotInfo, durable, false, false, null);
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, robotInfo, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes()); }
	        if ("robotstate".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, robotState, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("taskdata".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, taskData, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
	        } else if ("taskresult".equals(routeKey)) {
				//channel.exchangeDeclare(route ,rmqType, true, false, false, null);
	        	channel.basicPublish(route, taskResult, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
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
