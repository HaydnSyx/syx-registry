package cn.syx.registry.core.service;

import cn.syx.registry.core.cluster.Snapshot;
import cn.syx.registry.core.model.InstanceMeta;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    InstanceMeta register(String serviceName, InstanceMeta serviceMeta);

    InstanceMeta unregister(String serviceName, InstanceMeta serviceMeta);

    List<InstanceMeta> getAllInstances(String serviceName);

    long renew(InstanceMeta serviceMeta, String... services);

    long version(String service);

    Map<String, Long> versions(String... services);
}
