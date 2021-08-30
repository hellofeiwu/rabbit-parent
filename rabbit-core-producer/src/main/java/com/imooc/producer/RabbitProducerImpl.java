package com.imooc.producer;

import com.imooc.Message;
import com.imooc.constant.MessageRecordStatus;
import com.imooc.pojo.MessageRecord;
import com.imooc.service.MessageRecordService;
import com.imooc.utils.JsonUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RabbitProducerImpl implements RabbitProducer {

    @Autowired
    private RabbitTemplatePool rabbitTemplatePool;

    @Autowired
    private MessageRecordService messageRecordService;

    @Override
    public void rapidSend(Message message) {
        send(message);
    }

    private void send(Message message) {
        // 为了可以使用多线程来发送消息，这里使用了一个异步线程池
        AsyncThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                CorrelationData correlationData = new CorrelationData(
                        message.getMessageId()
                                + "#"
                                + System.currentTimeMillis()
                                + "#"
                                + message.getMessageType()
                );
                String exchange = message.getExchange();
                String routingKey = message.getRoutingKey();

                // 不使用Autowire的方式来使用rabbitTemplate,
                // 而是从 RabbitTemplatePool 中获取一个新的 RabbitTemplate
                // 这样可以做到每一个 exchange 对应一个 RabbitTemplate
                // 池化封装 RabbitTemplate 的好处
                // 1. 提高发送的效率
                // 2. 可以根据不同的需求 自定义不同的 RabbitTemplate, 比如每一个 exchange 都对应有自己的 routingKey 规则
                RabbitTemplate rabbitTemplate = rabbitTemplatePool.getRabbitTemplate(message);
                rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
                System.out.println("send message to rabbitmq, messageId: " + message.getMessageId());
            }
        });
    }

    @Override
    public void confirmSend(Message message) {
        send(message);
    }

    @Override
    public void reliantSend(Message message) {
        MessageRecord messageRecord = messageRecordService.queryById(message.getMessageId());
        if (messageRecord == null) {
            // 1. 在DB中创建一条消息记录
            messageRecord = new MessageRecord();
            messageRecord.setMessageId(message.getMessageId());
            String messageString = JsonUtils.objectToJson(message);
            messageRecord.setMessage(messageString);
            messageRecord.setStatus(MessageRecordStatus.SENDING.code);
            // tryCount在最开始不用设置
            Date now = new Date();
            messageRecord.setNextRetry(DateUtils.addMinutes(now, 1)); // 设置为添加1分钟
            messageRecord.setCreateTime(now);
            messageRecord.setUpdateTime(now);
            messageRecordService.insert(messageRecord);
        }

        // 2. 发送消息
        send(message);
    }

    @Override
    public void sendMessages() {

    }
}
