package com.imooc;

import java.util.HashMap;
import java.util.Map;

public class Message {

    // 消息的唯一ID
    private String messageId;

    // 这里的exchange都是topic类型的
    private String exchange;

    // 路由规则
    private String routingKey = "";

    // 消息的附加属性
    private Map<String, Object> attributes = new HashMap<>();

    // 延迟消息的毫秒数
    private int delayMillis;

    // 消息类型：默认为 confirm 类型
    private String messageType = MessageType.CONFIRM;

    public Message(String messageId, String exchange, String routingKey, Map<String, Object> attributes, int delayMillis, String messageType) {
        this.messageId = messageId;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.attributes = attributes;
        this.delayMillis = delayMillis;
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public int getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
