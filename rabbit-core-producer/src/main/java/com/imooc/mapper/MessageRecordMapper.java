package com.imooc.mapper;

import com.imooc.pojo.MessageRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface MessageRecordMapper {
    int insert(MessageRecord record);
    void updateMessageRecordStatus(@Param("messageId") String messageId,
                                   @Param("status") Integer status,
                                   @Param("updateTime") Date updateTime);
}
