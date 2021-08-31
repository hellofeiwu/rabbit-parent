package com.imooc;

import com.imooc.producer.ProducerClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest {
    @Autowired
    private ProducerClient producerClient;

    @Test
    public void testProducerClient() throws InterruptedException {
        for(int i = 0 ; i < 1; i ++) {
            String uniqueId = UUID.randomUUID().toString();
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("name", "张三");
            attributes.put("age", "18");
            Message message = new Message(
                    uniqueId,
                    "exchange-2",
                    "springboot.abc",
                    attributes,
                    0,
                    MessageType.RELIANT);
            producerClient.send(message);
        }

        Thread.sleep(100000);
    }
}
