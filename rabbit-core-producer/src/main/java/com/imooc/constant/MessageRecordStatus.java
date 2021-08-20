package com.imooc.constant;

/**
 * DB中的消息状态
 */
public enum MessageRecordStatus {
    SENDING(0),
    SEND_OK(1),
    SEND_FAIL(2),
    SEND_FAIL_A_MOMENT(3); // 带有特殊情况的 fail

    public Integer code;

    MessageRecordStatus(Integer code) {
        this.code = code;
    }
}
