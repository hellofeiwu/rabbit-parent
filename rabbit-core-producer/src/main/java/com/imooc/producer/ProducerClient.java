package com.imooc.producer;

import com.imooc.Message;
import com.imooc.MessageProducer;
import com.imooc.MessageType;
import com.imooc.SendCallback;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProducerClient implements MessageProducer {

    @Autowired
    private RabbitProducer rabbitProducer;

    @Override
    public void send(Message message, SendCallback sendCallback) {

    }

    @Override
    public void send(Message message) {
        Preconditions.checkNotNull(message.getExchange());
        String messageType = message.getMessageType();
        switch (messageType) {
            case MessageType.RAPID:
                rabbitProducer.rapidSend(message);
                break;
            case MessageType.CONFIRM:
                rabbitProducer.confirmSend(message);
                break;
            case MessageType.RELIANT:
                rabbitProducer.reliantSend(message);
                break;
            default:
                break;
        }

    }

    @Override
    public void send(List<Message> messages) {

    }
}
