package com.imooc.pojo;

import com.imooc.Message;
import lombok.Data;

import java.util.Date;

@Data
public class MessageRecord {
    private String messageId;
    private Message message;
    private Integer tryCount;
    private Integer status;
    private Date nextRetry;
    private Date createTime;
    private Date updateTime;
}
