package cn.syx.registry.server.service;

import cn.syx.registry.core.model.RegistryInstanceMeta;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    RegistryInstanceMeta register(String serviceName, RegistryInstanceMeta serviceMeta);

    RegistryInstanceMeta unregister(String serviceName, RegistryInstanceMeta serviceMeta);

    List<RegistryInstanceMeta> getAllInstances(String serviceName);

    long renew(RegistryInstanceMeta serviceMeta, String... services);

    long version(String service);

    Map<String, Long> versions(String... services);
}
