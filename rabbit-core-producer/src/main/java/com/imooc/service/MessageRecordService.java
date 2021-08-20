package com.imooc.service;

import com.imooc.pojo.MessageRecord;

public interface MessageRecordService {
    public int insert(MessageRecord messageRecord);
    public void success(String messageId);
}
