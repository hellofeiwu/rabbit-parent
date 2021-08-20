package com.imooc.service.impl;

import com.imooc.constant.MessageRecordStatus;
import com.imooc.mapper.MessageRecordMapper;
import com.imooc.pojo.MessageRecord;
import com.imooc.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MessageRecordServiceImpl implements MessageRecordService {
    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Override
    public int insert(MessageRecord messageRecord) {
        return messageRecordMapper.insert(messageRecord);
    }

    public void success(String messageId) {
        messageRecordMapper.updateMessageRecordStatus(messageId,
                MessageRecordStatus.SEND_OK.code,
                new Date());
    }
}
