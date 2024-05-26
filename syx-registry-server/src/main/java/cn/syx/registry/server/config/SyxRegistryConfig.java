package cn.syx.registry.server.config;

import cn.syx.registry.server.cluster.Cluster;
import cn.syx.registry.server.cluster.Election;
import cn.syx.registry.server.health.HealthChecker;
import cn.syx.registry.server.health.SyxHealthChecker;
import cn.syx.registry.server.service.RegistryService;
import cn.syx.registry.server.service.SyxRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public Election election() {
        return new Election();
    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired SyxRegistryConfigProperties properties,
                           @Autowired Election election) {
        return new Cluster(properties, election);
    }
}
