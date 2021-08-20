package com.imooc.producer;

import com.imooc.Message;

/**
 * 具体发送不同类型消息的接口
 */
public interface RabbitProducer {
    void rapidSend(Message message);
    void confirmSend(Message message);
    void reliantSend(Message message);
    void sendMessages();
}
