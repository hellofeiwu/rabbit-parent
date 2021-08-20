package com.imooc.producer;

import com.google.common.base.Preconditions;
import com.imooc.Message;
import com.imooc.MessageType;
import com.imooc.service.MessageRecordService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RabbitTemplatePool implements RabbitTemplate.ConfirmCallback {
    @Autowired
    private MessageRecordService messageRecordService;

    // 因为发送消息是在多线程的情况下进行的，所以要使用线程安全的Map
    private Map<String, RabbitTemplate> rabbitTemplateMap = new ConcurrentHashMap<>();

    public RabbitTemplate getRabbitTemplate(Message message) {
        Preconditions.checkNotNull(message);
        String exchange = message.getExchange();
        RabbitTemplate rabbitTemplate = rabbitTemplateMap.get(exchange);
        // 如果存在的话 就直接返回获取到的
        if (rabbitTemplate != null) {
            return rabbitTemplate;
        }

        // 如果不存在的话 就立即创建一个新的
        System.out.println("rabbitTemplate with exchange: "
                + exchange
                + " doesn't exist, will create one now."
        );

        RabbitTemplate newRabbitTemplate = new RabbitTemplate();

        //	TODO: 添加序列化反序列化 和 converter message对象

        String messageType = message.getMessageType();
        if (!MessageType.RAPID.equals(messageType)) {
            newRabbitTemplate.setConfirmCallback(this);
        }

        rabbitTemplateMap.putIfAbsent(exchange, newRabbitTemplate);

        return newRabbitTemplate;
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String[] stringArr = correlationData.getId().split("#");
        String messageId = stringArr[0];
        Long sendTime = Long.valueOf(stringArr[1]);

        if (ack) {
            messageRecordService.success(messageId);
            System.out.println("message sent OK, messageId: "
                                + messageId
                                + ", sendTime: "
                                + sendTime
            );
        }else {
            System.out.println("message sent FAIL, messageId: "
                    + messageId
                    + ", sendTime: "
                    + sendTime
            );
        }
    }
}
