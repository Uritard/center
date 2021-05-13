package com.yjh.etl.common.MQ.consumer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.*;
import com.yjh.etl.common.MQ.HttpRequest;
import com.yjh.etl.common.thread.TaskExecutePool;
import com.yjh.etl.commons.result.Result;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * task 包信息消费者
 *
 * @author stcstc00
 */
@Component
public class TaskMessageClient extends BaseTaskClient {

    private Logger logger = LoggerFactory.getLogger(TaskMessageClient.class);

    private static final String TASK_QUEUE_NAME = "rmq.taskrquename";// 队列名

    //@Autowired
    //private DataUploadFileService service;// 上传数据业务类

    //@Resource
    //private DataSynchronizationDao dataSynchronizationDao;

    private MqClient mqClient;

    //@PostConstruct
    public void startClient() {
        logger.warn("startClient");
        TaskExecutePool.getInstance().execute(new Runnable() {
            public void run() {
                mqClient = new MqClient();
                try {
                    mqClient.start();
                } catch (Exception e) {
                    logger.error("mqClient start error", e);
                }
            }
        });
    }

    @PreDestroy
    private void stopClient() {
        System.out.println("stopClient");
        logger.warn("stopClient");
        if (mqClient != null) {
            try {
                mqClient.stop();
            } catch (Exception e) {
                logger.error("mqClient stop error", e);
            }
        }
    }

    private class TaskMessageConsumer extends DefaultConsumer {

        public TaskMessageConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
            super.handleConsumeOk(consumerTag);
            logger.debug("handleConsumeOk: consumer - {}", consumerTag);
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            super.handleCancelOk(consumerTag);
            logger.debug("handleCancelOk: consumer - {}", consumerTag);
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            super.handleCancel(consumerTag);
            logger.debug("handleCancel: consumer - {}", consumerTag);
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            super.handleShutdownSignal(consumerTag, sig);
            logger.info("handleShutdownSignal: consumer - {}", consumerTag);
        }

        @Override
        public void handleRecoverOk(String consumerTag) {
            super.handleRecoverOk(consumerTag);
            logger.info("handleRecoverOk: consumer - {}", consumerTag);
        }

        /*
        * 收到消息后用来处理消息的回调对象
        * */
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                   byte[] body) throws IOException {
            logger.info("handleDelivery: consumer({}), envelope({})", consumerTag, envelope);

            try {
                String message = new String(body, "UTF-8");
                doWork(message);
            } catch (Exception e) {
                logger.error("doWork in handleDelivery error", e);
            } finally {
                getChannel().basicAck(envelope.getDeliveryTag(), false);//消费者挂掉,发送一个消息确认(回执)
            }
        }
    }

    private class MqClient {
        AtomicBoolean started = new AtomicBoolean(false);
        Connection connection;
        Channel channel;

        void start() throws Exception {
            if (started.compareAndSet(false, true)) {
                Connection connection = getConnection(true);
                final Channel channel = connection.createChannel();//建立信道
                channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
                // 每次从队列中获取数量,在返回确认回执前,不要向消费者发送新消息,而是把消息发给下一个空闲的消费者
                channel.basicQos(1);
                channel.basicConsume(TASK_QUEUE_NAME, false/*非自动应答,手动应答*/, new TaskMessageConsumer(channel));
                logger.warn("MqClient started");
            }
        }

        void stop() throws Exception {
            if (started.compareAndSet(true, false)) {
                if (channel != null) {
                    channel.close();
                    channel = null;
                }

                if (connection != null) {
                    connection.close();
                    connection = null;
                }

                System.out.println("MqClient stopped");
                logger.warn("MqClient stopped");
            }
        }

    }

//    public void initClient() throws IOException, TimeoutException {
//        if (!flag) {
//            Connection connection = getConnection(true);
//            final Channel channel = connection.createChannel();
//            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
//            // 每次从队列中获取数量
//            channel.basicQos(1);
//
//            final Consumer consumer = new DefaultConsumer(channel) {
//                @Override
//                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
//                                           byte[] body) throws IOException {
//                    String message = new String(body, "UTF-8");
//                    try {
//                        doWork(message);
//                    } catch (Exception e) {
//                        Log4jUtil.printErrorlog(TaskClientController.class,
//                                Log4jPojo.TaskClient, "TaskClientController dowork error", e);
//                    } finally {
//                        channel.basicAck(envelope.getDeliveryTag(), false);
//                    }
//                }
//            };
//            // 监听队列，手动返回完成 注：第二个参数值为false代表关闭RabbitMQ的自动应答机制，改为手动应答。
//            channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
//        }
//        flag = true;
//    }

    /**
     * 调用数据上传
     *
     * @param taskMessage
     * @throws Exception
     */
    private void doWork(String taskMessage) throws Exception {
        logger.info("Received task message: {}", taskMessage);
        if (taskMessage == null || taskMessage.isEmpty())
            return;

        JSONObject jObj = JSONObject.parseObject(taskMessage);
        String taskId = jObj.getString("TaskID");
        String url = jObj.getString("url");
        if (StringUtils.isBlank(taskId)) throw new IllegalArgumentException("empty taskId");
        if (StringUtils.isBlank(url)) throw new IllegalArgumentException("empty url");

        Result results = new Result();
        try {
            //service.taskDataUploadAdd(url, taskId, results);
        } catch (Exception e) {
            logger.error("taskDataUploadAdd error", e);
            if (results.getData() instanceof List) {
                List<String> pngAddress = (List<String>) results.getData();
                //ModelResults resultsPic = service.taskErrorAdd(taskId, url, pngAddress);
            } else {
                //ModelResults resultsPic = service.taskErrorAdd(taskId, url, null);
            }
        } finally {
            // 同步工区数据
            try {
                //Long stationId = dataSynchronizationDao.queryStationId(taskId);

                Map<String, Object> params = new HashMap<>();
                //params.put("stationId", stationId);
                JSONObject requestParam = new JSONObject();
                requestParam.put("params", params);

                JSONObject obj = HttpRequest.httpPost(
                        "synchronousDataRequestPath",
                        requestParam, false);

                if (obj != null && !"200".equals(obj.get("code").toString())) {
                    logger.error("调用工区synchronousDataByStationId接口失败！原因：" + obj.get("message"));
                }
            } catch (Exception e) {
                logger.error("同步工区数据失败！", e);
            }
        }
    }
}
