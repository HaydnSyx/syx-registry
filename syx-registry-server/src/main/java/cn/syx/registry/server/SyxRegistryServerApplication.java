package cn.syx.registry.server;

import cn.syx.registry.server.config.SyxRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SyxRegistryConfigProperties.class})
public class SyxRegistryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRegistryServerApplication.class, args);
    }

}
