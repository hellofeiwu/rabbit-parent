package com.imooc;

import com.imooc.exception.MessageRunTimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MessageBuilder 建造者模式
 */
public class MessageBuilder {
    private String messageId;
    private String topic;
    private String routingKey = "";
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private int delayMillis;
    private String messageType = MessageType.CONFIRM;

    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    public MessageBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public MessageBuilder withTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public MessageBuilder withRoutingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    public MessageBuilder withAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public MessageBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public MessageBuilder withDelayMillis(int delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    public MessageBuilder withMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public Message build() {
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
        }

        if (topic == null) {
            throw new MessageRunTimeException("topic is null");
        }

        return new Message(messageId, topic, routingKey, attributes, delayMillis, messageType);
    }
}
