package cn.syx.registry.server.controller;

import cn.syx.registry.server.cluster.Cluster;
import cn.syx.registry.core.model.Server;
import cn.syx.registry.server.cluster.Snapshot;
import cn.syx.registry.core.model.RegistryInstanceMeta;
import cn.syx.registry.server.service.RegistryService;
import cn.syx.registry.server.service.SyxRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class SyxRegistryController {

    @Autowired
    private RegistryService registryService;
    @Autowired
    private Cluster cluster;

    @RequestMapping("/reg")
    public RegistryInstanceMeta registry(@RequestParam("service") String service,
                                         @RequestBody RegistryInstanceMeta instance) {
        log.info("registry service: {}, instance: {}", service, instance);
        checkLeader();
        registryService.register(service, instance);
        return instance;
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new RuntimeException("current server is not leader, can not do this operation! leader is " + cluster.leader().getUrl());
        }
    }

    @RequestMapping("/unreg")
    public RegistryInstanceMeta unregister(@RequestParam("service") String service,
                                           @RequestBody RegistryInstanceMeta instance) {
        log.info("unregister service: {}, instance: {}", service, instance);
        checkLeader();
        registryService.unregister(service, instance);
        return instance;
    }

    @RequestMapping("/findAll")
    public List<RegistryInstanceMeta> findAllInstances(@RequestParam("service") String service) {
        log.info("find all instances of service: {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renews")
    public long renew(@RequestParam("services") String services,
                      @RequestBody RegistryInstanceMeta instance) {
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public long version(@RequestParam("service") String service) {
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam("services") String services) {
        return registryService.versions(services.split(","));
    }

    @RequestMapping("/info")
    public Server info() {
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> info: {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ===> leader: {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/sl")
    public Server sl() {
        cluster.self().setLeader(true);
        log.info(" ===> leader: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        return SyxRegistryService.snapshot();
    }
}
