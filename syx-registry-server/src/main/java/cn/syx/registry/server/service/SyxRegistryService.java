package cn.syx.registry.server.service;

import cn.syx.registry.server.cluster.Snapshot;
import cn.syx.registry.core.model.RegistryInstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SyxRegistryService implements RegistryService {

    private static final MultiValueMap<String, RegistryInstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    private static final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public static final Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public static final AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized RegistryInstanceMeta register(String serviceName, RegistryInstanceMeta serviceMeta) {
        List<RegistryInstanceMeta> metas = REGISTRY.get(serviceName);
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
    public synchronized RegistryInstanceMeta unregister(String serviceName, RegistryInstanceMeta serviceMeta) {
        List<RegistryInstanceMeta> metas = REGISTRY.get(serviceName);

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
    public List<RegistryInstanceMeta> getAllInstances(String serviceName) {
        return REGISTRY.get(serviceName);
    }

    @Override
    public long renew(RegistryInstanceMeta serviceMeta, String... services) {
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

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, RegistryInstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(Snapshot snapshot) {
        if (Objects.isNull(snapshot)) {
            return -1;
        }

        REGISTRY.clear();
        VERSIONS.clear();
        TIMESTAMPS.clear();

        REGISTRY.addAll(snapshot.getRegistry());
        VERSIONS.putAll(snapshot.getVersions());
        TIMESTAMPS.putAll(snapshot.getTimestamps());
        VERSION.set(snapshot.getVersion());

        return snapshot.getVersion();
    }
}
