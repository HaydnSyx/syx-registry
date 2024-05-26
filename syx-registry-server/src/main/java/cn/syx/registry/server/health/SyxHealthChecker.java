package cn.syx.registry.server.health;

import cn.syx.registry.core.model.RegistryInstanceMeta;
import cn.syx.registry.server.service.RegistryService;
import cn.syx.registry.server.service.SyxRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SyxHealthChecker implements HealthChecker {

    private RegistryService registryService;

    private final int timeout = 20000;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public SyxHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(
            () -> {
                log.info("===> health checker is running...");
                long now = System.currentTimeMillis();
                SyxRegistryService.TIMESTAMPS.forEach((k, v) -> {
                    if (now - v > timeout) {
                        log.info("===> service {} is not available", k);
                        String[] services = k.split("@");
                        String service = services[0];
                        String url = services[1];
                        RegistryInstanceMeta instance = RegistryInstanceMeta.from(url);
                        registryService.unregister(service, instance);
                        SyxRegistryService.TIMESTAMPS.remove(k);
                    }
                });
            },
            10,
            10,
            TimeUnit.SECONDS
        );
    }

    @Override
    public void stop() {

    }
}
