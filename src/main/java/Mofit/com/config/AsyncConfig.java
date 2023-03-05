package Mofit.com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("MyExecutor-");
        executor.setAwaitTerminationSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setCorePoolSize(20);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(50);
        return executor;
    }
}
