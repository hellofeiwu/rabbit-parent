package com.imooc.service.impl;

import com.imooc.constant.MessageRecordStatus;
import com.imooc.mapper.MessageRecordMapper;
import com.imooc.pojo.MessageRecord;
import com.imooc.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MessageRecordServiceImpl implements MessageRecordService {
    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Override
    public int insert(MessageRecord messageRecord) {
        return messageRecordMapper.insert(messageRecord);
    }

    public MessageRecord queryById(String messageId) {
        return messageRecordMapper.selectByPrimaryKey(messageId);
    }

    public void success(String messageId) {
        messageRecordMapper.updateMessageRecordStatus(messageId,
                MessageRecordStatus.SEND_OK.code,
                new Date());
    }

    public void fail(String messageId) {
        messageRecordMapper.updateMessageRecordStatus(messageId,
                MessageRecordStatus.SEND_FAIL.code,
                new Date());
    }

    public List<MessageRecord> fetchTimeOutMessage4Retry(Integer status) {
        return messageRecordMapper.queryMessageRecordStatus4Timeout(status);
    }

    public int updateTryCount(String messageId) {
        return messageRecordMapper.update4TryCount(messageId, new Date());
    }
}
