package com.imooc;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Message {

    // 消息的唯一ID
    private String messageId;

    // 这里是指exchange, 因为这里用的exchange都是topic类型的，所以就直接写成topic了
    private String topic;

    // 路由规则
    private String routingKey = "";

    // 消息的附加属性
    private Map<String, Object> attributes = new HashMap<>();

    // 延迟消息的毫秒数
    private int delayMillis;

    // 消息类型：默认为 confirm 类型
    private String messageType = MessageType.CONFIRM;

    public Message(String messageId, String topic, String routingKey, Map<String, Object> attributes, int delayMillis, String messageType) {
        this.messageId = messageId;
        this.topic = topic;
        this.routingKey = routingKey;
        this.attributes = attributes;
        this.delayMillis = delayMillis;
        this.messageType = messageType;
    }
}
