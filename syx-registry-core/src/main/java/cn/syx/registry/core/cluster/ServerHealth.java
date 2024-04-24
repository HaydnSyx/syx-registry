package cn.syx.registry.core.cluster;

import cn.syx.registry.core.http.HttpInvoker;
import cn.syx.registry.core.service.SyxRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerHealth {

    private final Cluster cluster;
    private final Election election;
    private final int interval = 5000;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public ServerHealth(Cluster cluster, Election election) {
        this.cluster = cluster;
        this.election = election;
    }

    public void checkServerHealth() {
        // Check server health

        executor.scheduleAtFixedRate(() -> {
            try {
                // 1.更新服务状态
                updateServers();
                // 2.选举leader
                electLeader();
                // 3.同步leader的snapshot
                syncSnapshotFromLeader();
            } catch (Exception e) {
                log.error("===> check server health fail: ", e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void electLeader() {
        election.doElectLeader(cluster.getServers());
    }

    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            try {
                log.info("===> sync snapshot from leader version: {}, self version:{}", leader.getVersion(), self.getVersion());
                Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
                log.info("===> sync snapshot from leader success: {}", snapshot);
                SyxRegistryService.restore(snapshot);
            } catch (Exception e) {
                log.error("===> sync snapshot from leader fail: {}", leader, e);
            }
        }
    }

    private void updateServers() {
        cluster.getServers().stream().parallel().forEach(server -> {
            try {
                if (Objects.equals(server, cluster.self())) {
                    return;
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
        });
    }


}
