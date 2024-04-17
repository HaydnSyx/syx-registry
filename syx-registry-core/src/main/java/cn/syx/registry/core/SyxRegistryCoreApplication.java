package cn.syx.registry.core;

import cn.syx.registry.core.config.SyxRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SyxRegistryConfigProperties.class})
public class SyxRegistryCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyxRegistryCoreApplication.class, args);
    }

}
