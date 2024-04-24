package cn.syx.registry.core.cluster;

import cn.syx.registry.core.config.SyxRegistryConfigProperties;
import cn.syx.registry.core.http.HttpInvoker;
import cn.syx.registry.core.service.SyxRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Cluster {

    @Value("${server.port}")
    private int port;
    private String host;

    private Server self;

    private final Election election;

    private final SyxRegistryConfigProperties registryConfigProperties;

    @Getter
    private List<Server> servers;

    public Cluster(SyxRegistryConfigProperties registryConfigProperties, Election election) {
        this.registryConfigProperties = registryConfigProperties;
        this.election = election;
    }

    public void init() {
        log.debug("Cluster init...");
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        initServers();

        ServerHealth serverHealth = new ServerHealth(this, election);
        serverHealth.checkServerHealth();
    }

    private void initServers() {
        servers = new ArrayList<>();
        self = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info("===> self: {}", self);
        servers.add(self);

        for (String serverUrl : registryConfigProperties.getServers()) {
            if (serverUrl.contains("localhost")) {
                serverUrl = serverUrl.replace("localhost", host);
            } else if (serverUrl.contains("127.0.0.1")) {
                serverUrl = serverUrl.replace("127.0.0.1", host);
            }

            if (Objects.equals(serverUrl, self.getUrl())) {
                continue;
            }

            Server server = new Server();
            server.setUrl(serverUrl);
            server.setStatus(false);
            server.setLeader(false);
            server.setVersion(-1L);

            servers.add(server);
        }
    }

    public Server self() {
        this.self.setVersion(SyxRegistryService.VERSION.get());
        return this.self;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }
}
