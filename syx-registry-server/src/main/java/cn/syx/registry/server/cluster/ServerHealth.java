package cn.syx.registry.server.cluster;

import cn.syx.registry.core.model.Server;
import cn.syx.registry.core.util.OkHttpClientHelper;
import cn.syx.registry.server.service.SyxRegistryService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerHealth {

    private final Cluster cluster;
    private final Election election;
    private final OkHttpClient client;
    private final int interval = 5000;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public ServerHealth(Cluster cluster, Election election) {
        this.cluster = cluster;
        this.election = election;
        this.client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .build();
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
                Snapshot snapshot = OkHttpClientHelper.httpGet(this.client, leader.getUrl() + "/snapshot", Snapshot.class);
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

                Server serverInfo = OkHttpClientHelper.httpGet(this.client, server.getUrl() + "/info", Server.class);
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
