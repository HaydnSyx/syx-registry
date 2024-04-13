package cn.syx.registry.core.service;

import cn.syx.registry.core.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SyxRegistryService implements RegistryService {

    private static final MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    private static final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public static final Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    private static final AtomicLong VERSION = new AtomicLong(0);

    @Override
    public InstanceMeta register(String serviceName, InstanceMeta serviceMeta) {
        List<InstanceMeta> metas = REGISTRY.get(serviceName);
        if (Objects.nonNull(metas) && !metas.isEmpty()) {
            if (metas.contains(serviceMeta)) {
                log.info("===> service {} already registered", serviceName);
                serviceMeta.setStatus(true);
                return serviceMeta;
            }
        }

        log.info("===> register service {} with instance {}", serviceName, serviceMeta.toUrl());
        REGISTRY.add(serviceName, serviceMeta);
        serviceMeta.setStatus(true);
        renew(serviceMeta, serviceName);
        VERSIONS.put(serviceName, VERSION.incrementAndGet());
        return serviceMeta;
    }

    @Override
    public InstanceMeta unregister(String serviceName, InstanceMeta serviceMeta) {
        List<InstanceMeta> metas = REGISTRY.get(serviceName);

        if (Objects.isNull(metas) || metas.isEmpty()) {
            return null;
        }

        if (metas.contains(serviceMeta)) {
            log.info("===> unregister service {} with instance {}", serviceName, serviceMeta.toUrl());
            metas.remove(serviceMeta);
            serviceMeta.setStatus(false);
            renew(serviceMeta, serviceName);
            VERSIONS.put(serviceName, VERSION.incrementAndGet());
            return serviceMeta;
        }
        return null;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String serviceName) {
        return REGISTRY.get(serviceName);
    }

    @Override
    public long renew(InstanceMeta serviceMeta, String... services) {
        long time = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + serviceMeta.toUrl(), time);
//        VERSIONS.put(service, VERSION.incrementAndGet());
        }
        return time;
    }

    @Override
    public long version(String service) {
        return VERSIONS.getOrDefault(service, 0L);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        Map<String, Long> versions = new HashMap<>();
        for (String service : services) {
            versions.put(service, version(service));
        }
        return versions;
    }
}
