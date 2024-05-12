package cn.syx.registry.client.config;

import lombok.Data;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "syx.registry.client")
public class SyxRegistryClientProperties {

    private String servers;

    private int maxConnections = 5;
    private int keepLiveSec = 60;
    private int connectionTimeout = 500;
    private int socketTimeout = 500;

    private int initDelayCheckTime = 5000;
    private int delayCheckGapTime = 5000;
}
