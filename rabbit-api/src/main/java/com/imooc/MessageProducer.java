package com.imooc;

import java.util.List;

public interface MessageProducer {
    // 带有回调函数的send方法
    void send(Message message, SendCallback sendCallback);
    void send(Message message);
    // 批量发送的send方法
    void send(List<Message> messages);
}
