package cn.syx.registry.core;

import cn.syx.registry.core.model.InstanceMeta;
import cn.syx.registry.core.service.RegistryService;
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

    @RequestMapping("/reg")
    public InstanceMeta registry(@RequestParam("service") String service,
                                 @RequestBody InstanceMeta instance) {
        log.info("registry service: {}, instance: {}", service, instance);
        registryService.register(service, instance);
        return instance;
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam("service") String service,
                                   @RequestBody InstanceMeta instance) {
        log.info("unregister service: {}, instance: {}", service, instance);
        registryService.unregister(service, instance);
        return instance;
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam("service") String service) {
        log.info("find all instances of service: {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestParam("services") String services,
                      @RequestBody InstanceMeta instance) {
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
}
