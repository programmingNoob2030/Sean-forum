package sim.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 必须加这个注解，否则 @Async 不起作用
public class ThreadPoolConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：当前线程池维护的最小线程数量
        executor.setCorePoolSize(5);
        // 最大线程数：任务过多时能开辟的最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量：等待执行的任务排队的地方
        executor.setQueueCapacity(200);
        // 线程前缀名：方便以后你在控制台调试日志，一眼看出是异步任务在跑
        executor.setThreadNamePrefix("ForumAsync-");
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);


        // 关键点：加上这一行
        // 当任务队列满且线程数达上限时，由“调用者线程”（比如执行点赞业务的 Controller 线程）自己执行任务
        // 这能有效防止消息丢失，并自动减缓请求速度
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;

    }
}