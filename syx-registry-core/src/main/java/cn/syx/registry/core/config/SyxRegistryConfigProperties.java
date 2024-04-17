package cn.syx.registry.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("syxregistry")
public class SyxRegistryConfigProperties {

    private List<String> servers;
}
