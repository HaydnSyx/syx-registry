package cn.syx.registry.core.cluster;

import cn.syx.registry.core.config.SyxRegistryConfigProperties;
import cn.syx.registry.core.http.HttpInvoker;
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

    private final SyxRegistryConfigProperties registryConfigProperties;

    @Getter
    private List<Server> servers;

    private final int timeout = 5000;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public Cluster(SyxRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    public void init() {
        log.debug("Cluster init...");
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        } catch (Exception e) {
            host = "127.0.0.1";
        }

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

        executor.scheduleAtFixedRate(() -> {
            try {
                updateServers();
                electLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void updateServers() {
        for (Server server : servers) {
            try {
                if (Objects.equals(server, self)) {
                    continue;
                }
                
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.info("===> health check server success: {}", serverInfo);
                if (Objects.nonNull(serverInfo)) {
                    server.setVersion(serverInfo.getVersion());
                    server.setLeader(serverInfo.isLeader());
                    server.setStatus(serverInfo.isStatus());
                }
            } catch (Exception e) {
                log.error("===> health check server fail: {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        }
    }

    private void electLeader() {
        List<Server> leaders = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        if (leaders.isEmpty()) {
            log.info("===> elect for no leader, self: {}", self);
            elect();
        } else if (leaders.size() > 1) {
            log.info("===> elect for multi leader, self: {}", self);
            elect();
        } else {
            log.info("===> no need elect leader: {}", leaders.get(0));
        }
    }

    private void elect() {
        // 1. 各种节点自己选，算法保证大家选的是同一个
        // 2. 外部有一个分布式锁，谁拿到锁，谁当主
        // 3. 通过分布式一致性算法选举
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            log.info(" ===>>> elect for leader: " + candidate);
        } else {
            log.error(" ===>>> elect failed for no leaders: " + servers);
        }
    }

    public Server self() {
        return this.self;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }
}
