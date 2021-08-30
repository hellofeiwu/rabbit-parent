package com.imooc.mapper;

import com.imooc.pojo.MessageRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MessageRecordMapper {
    int insert(MessageRecord record);
    MessageRecord selectByPrimaryKey(String messageId);
    void updateMessageRecordStatus(@Param("messageId") String messageId,
                                   @Param("status") Integer status,
                                   @Param("updateTime") Date updateTime);
    List<MessageRecord> queryMessageRecordStatus4Timeout(@Param("status") Integer status);
    int update4TryCount(@Param("messageId") String messageId, @Param("updateTime") Date updateTime);
}
