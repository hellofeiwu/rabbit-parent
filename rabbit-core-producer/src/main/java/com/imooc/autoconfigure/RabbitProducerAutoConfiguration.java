package com.imooc.autoconfigure;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装配
 */
@Configuration
@ComponentScan({"com.imooc.*"})
public class RabbitProducerAutoConfiguration {
}
