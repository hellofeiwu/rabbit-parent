package com.imooc.task;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.imooc.Message;
import com.imooc.annotation.ElasticJobConfig;
import com.imooc.constant.MessageRecordStatus;
import com.imooc.pojo.MessageRecord;
import com.imooc.producer.RabbitProducer;
import com.imooc.service.MessageRecordService;
import com.imooc.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ElasticJobConfig(
        name = "com.imooc.task.RetryMessageDataflowJob",
        cron = "0/5 * * * * ?",
        description = "可靠性投递重试任务",
        overwrite = true,
        shardingTotalCount = 1
)
public class RetryMessageDataflowJob implements DataflowJob<MessageRecord> {
    private static final int MAX_RETRY_COUNT = 3;


    @Autowired
    private MessageRecordService messageRecordService;

    @Autowired
    private RabbitProducer rabbitProducer;

    @Override
    public List<MessageRecord> fetchData(ShardingContext shardingContext) {
        List<MessageRecord> list = messageRecordService.fetchTimeOutMessage4Retry(MessageRecordStatus.SENDING.code);
        System.out.println("------->>>> 抓取到需要重试的消息个数：" + list.size());
        return list;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<MessageRecord> list) {
        list.forEach(messageRecord -> {
            String messageId = messageRecord.getMessageId();
            if (messageRecord.getTryCount() >= MAX_RETRY_COUNT) {
                messageRecordService.fail(messageId);
                System.out.println("------->>>> 消息重试最终失败，id: " + messageId);
            }else {
                messageRecordService.updateTryCount(messageId);
                Message message = JsonUtils.jsonToPojo(messageRecord.getMessage(), Message.class);
                rabbitProducer.reliantSend(message);
            }
        });
    }
}
