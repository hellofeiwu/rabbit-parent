package com.imooc.parser;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.imooc.annotation.ElasticJobConfig;
import com.imooc.autoconfigure.JobZookeeperProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;

/**
 * 这个类是在等所有 Spring Bean 都加载完成之后
 * 来做具体job的 config
 * 就是之前 MySimpleJobConfig 的部分
 */
public class ElasticJobConfParser implements ApplicationListener<ApplicationReadyEvent> {

    private JobZookeeperProperties jobZookeeperProperties;
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    public ElasticJobConfParser(JobZookeeperProperties jobZookeeperProperties, ZookeeperRegistryCenter zookeeperRegistryCenter) {
        this.jobZookeeperProperties = jobZookeeperProperties;
        this.zookeeperRegistryCenter = zookeeperRegistryCenter;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try{
            ApplicationContext applicationContext = applicationReadyEvent.getApplicationContext();
            Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(ElasticJobConfig.class);
            for(Map.Entry<String, Object> item : beanMap.entrySet()) {
                Object jobBean = item.getValue();
                Class<?> clazz = jobBean.getClass();
                String jobClassName = clazz.getName();

                // 处理带有特殊符号的 class
                if (jobClassName.indexOf("$") > 0) {
                    clazz = Class.forName(jobClassName.substring(0, jobClassName.indexOf("$")));
                }

                // 获取实现的接口名称（SimpleJob, DataflowJob, ScriptJob），用于判断是什么类型的job
                // 当这个类实现多个接口的时候，真正想找的接口不一定在数组的第0位，需要去循环判断
                String jobTypeInterfaceName = clazz.getInterfaces()[0].getSimpleName();

                // 获取配置项 ElasticJobConfig, 并将其中的值都拿出来
                ElasticJobConfig config = clazz.getAnnotation(ElasticJobConfig.class);

                // 为了防止 job 重名，所以修改一下job name
                String jobName = this.jobZookeeperProperties.getNamespace() + "." + config.name();
                String cron = config.cron();
                String shardingItemParameters = config.shardingItemParameters();
                String description = config.description();
                String jobParameter = config.jobParameter();
                String jobExceptionHandler = config.jobExceptionHandler();
                String executorServiceHandler = config.executorServiceHandler();

                String jobShardingStrategyClass = config.jobShardingStrategyClass();
                String eventTraceRdbDataSource = config.eventTraceRdbDataSource();
                String scriptCommandLine = config.scriptCommandLine();

                boolean failover = config.failover();
                boolean misfire = config.misfire();
                boolean overwrite = config.overwrite();
                boolean disabled = config.disabled();
                boolean monitorExecution = config.monitorExecution();
                boolean streamingProcess = config.streamingProcess();

                int shardingTotalCount = config.shardingTotalCount();
                int monitorPort = config.monitorPort();
                int maxTimeDiffSeconds = config.maxTimeDiffSeconds();
                int reconcileIntervalMinutes = config.reconcileIntervalMinutes();

                // 创建一个 SpringJobScheduler bean，这里的创建方法跟之前的不同
                // 因为现在 Spring 的 bean 都已经创建完成了
                BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
                factory.setInitMethodName("init");
                factory.setScope("prototype"); // 这里设置是表示这个bean会是多例模式

                // 1. 注入实际的任务实现类
                factory.addConstructorArgValue(jobBean);

                // 2. 注入zookeeper注册中心
                factory.addConstructorArgValue(this.zookeeperRegistryCenter);

                // 3. 注入LiteJobConfiguration
                factory.addConstructorArgValue(getLiteJobConfiguration(
                                                    jobClassName,
                                                    jobTypeInterfaceName,
                                                    jobName,
                                                    cron,
                                                    shardingTotalCount,
                                                    shardingItemParameters,
                                                    description,
                                                    jobParameter,
                                                    misfire,
                                                    failover,
                                                    monitorExecution,
                                                    overwrite,
                                                    streamingProcess,
                                                    scriptCommandLine,
                                                    reconcileIntervalMinutes,
                                                    disabled,
                                                    monitorPort,
                                                    maxTimeDiffSeconds,
                                                    jobShardingStrategyClass,
                                                    jobExceptionHandler,
                                                    executorServiceHandler));

                // 4. 可选 注入 JobEventRdbConfiguration 用来在DB中记录的数据源
                if (StringUtils.isNotBlank(eventTraceRdbDataSource)) {
                    // 创建 JobEventRdbConfiguration bean 跟之前的方法也不同
                    BeanDefinitionBuilder rdbFactory = BeanDefinitionBuilder.rootBeanDefinition(JobEventRdbConfiguration.class);
                    rdbFactory.addConstructorArgReference(eventTraceRdbDataSource);

                    factory.addConstructorArgValue(rdbFactory.getBeanDefinition());
                }

                // 5. 添加job listeners
                List<?> listeners = getTargetElasticJobListeners(config);
                factory.addConstructorArgValue(listeners);

                // 接下来是把 factory 也就是 SpringJobScheduler 注入到 Spring 容器中
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
                String registerBeanName = config.name() + "SpringJobScheduler";
                defaultListableBeanFactory.registerBeanDefinition(registerBeanName, factory.getBeanDefinition());

                // 将这个 bean 取出来 启动
                SpringJobScheduler springJobScheduler = (SpringJobScheduler) applicationContext.getBean(registerBeanName);
                springJobScheduler.init();
                System.out.println("------------>>>> 启动 elastic-job: " + jobName);
            }
            System.out.println("------------>>>> 共计启动 elastic-job 个数: " + beanMap.size());
        }catch (Exception e) {
            System.out.println("------------>>>> elastic-job 启动异常，系统强制退出， 异常 message：" + e.getMessage());
            System.exit(1);
        }
    }

    private LiteJobConfiguration getLiteJobConfiguration(
            String jobClassName,
            String jobTypeInterfaceName,
            String jobName,
            String cron,
            int shardingTotalCount,
            String shardingItemParameters,
            String description,
            String jobParameter,
            boolean misfire,
            boolean failover,
            boolean monitorExecution,
            boolean overwrite,
            boolean streamingProcess,
            String scriptCommandLine,
            int reconcileIntervalMinutes,
            boolean disabled,
            int monitorPort,
            int maxTimeDiffSeconds,
            String jobShardingStrategyClass,
            String jobExceptionHandler,
            String executorServiceHandler
    ) {
        JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                .newBuilder(jobName, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters)
                .description(description)
                .failover(failover)
                .jobParameter(jobParameter)
                .misfire(misfire)
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandler)
                .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandler)
                .build();

        JobTypeConfiguration jobTypeConfiguration = null;

        switch (jobTypeInterfaceName) {
            case "SimpleJob":
                jobTypeConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, jobClassName);
                break;
            case "DataflowJob":
                jobTypeConfiguration = new DataflowJobConfiguration(jobCoreConfiguration, jobClassName, streamingProcess);
                break;
            case "ScriptJob":
                jobTypeConfiguration = new ScriptJobConfiguration(jobCoreConfiguration, scriptCommandLine);
                break;
            default:
                break;
        }


        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration
                .newBuilder(jobTypeConfiguration)
                .overwrite(overwrite)
                .disabled(disabled)
                .monitorPort(monitorPort)
                .monitorExecution(monitorExecution)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .jobShardingStrategyClass(jobShardingStrategyClass)
                .reconcileIntervalMinutes(reconcileIntervalMinutes)
                .build();

        return liteJobConfiguration;

    }

    private List<BeanDefinition> getTargetElasticJobListeners(ElasticJobConfig config) {
        List<BeanDefinition> result = new ManagedList<BeanDefinition>(2);
        String listeners = config.listener();
        if (org.springframework.util.StringUtils.hasText(listeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listeners);
            factory.setScope("prototype");
            result.add(factory.getBeanDefinition());
        }

        String distributedListeners = config.distributedListener();
        long startedTimeoutMilliseconds = config.startedTimeoutMilliseconds();
        long completedTimeoutMilliseconds = config.completedTimeoutMilliseconds();

        if (org.springframework.util.StringUtils.hasText(distributedListeners)) {
            BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListeners);
            factory.setScope("prototype");
            factory.addConstructorArgValue(startedTimeoutMilliseconds);
            factory.addConstructorArgValue(completedTimeoutMilliseconds);
            result.add(factory.getBeanDefinition());
        }
        return result;
    }
}
