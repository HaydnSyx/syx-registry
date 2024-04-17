package cn.syx.registry.core.config;

import cn.syx.registry.core.cluster.Cluster;
import cn.syx.registry.core.health.HealthChecker;
import cn.syx.registry.core.health.SyxHealthChecker;
import cn.syx.registry.core.service.RegistryService;
import cn.syx.registry.core.service.SyxRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SyxRegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new SyxRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new SyxHealthChecker(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired SyxRegistryConfigProperties properties) {
        return new Cluster(properties);
    }
}
