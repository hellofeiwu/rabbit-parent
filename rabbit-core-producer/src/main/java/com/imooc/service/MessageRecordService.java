package com.imooc.service;

import com.imooc.pojo.MessageRecord;

import java.util.List;

public interface MessageRecordService {
    public int insert(MessageRecord messageRecord);
    public MessageRecord queryById(String messageId);
    public void success(String messageId);
    public void fail(String messageId);
    public List<MessageRecord> fetchTimeOutMessage4Retry(Integer status);
    public int updateTryCount(String messageId);
}
