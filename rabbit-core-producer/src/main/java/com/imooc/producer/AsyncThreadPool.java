package com.imooc.producer;

import java.util.concurrent.*;

public class AsyncThreadPool {
    private static final int THREAD_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int QUEUE_SIZE = 10000;

    private static ExecutorService senderAsync = new ThreadPoolExecutor(
            THREAD_SIZE,
            QUEUE_SIZE,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
            // 开启线程成功的方法
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("rabbitmq_client_async_sender");
                    return t;
                }
            },
            // 开启线程失败后的方法
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    System.out.println("异步线程开启失败, runnable: " + r + ", executor: " + executor);
                }
            }
    );

    public static void submit(Runnable runnable) {
        senderAsync.submit(runnable);
    }
}
