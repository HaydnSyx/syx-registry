package cn.syx.registry.client.config;

import cn.syx.registry.client.SyxRegistryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SyxRegistryClientProperties.class})
public class SyxRegistryClientAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SyxRegistryClient syxRegistryClient(@Autowired SyxRegistryClientProperties properties) {
        return new SyxRegistryClient(properties);
    }
}
