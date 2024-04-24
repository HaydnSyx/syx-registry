package cn.syx.registry.core.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Election {

    public void doElectLeader(List<Server> servers) {
        List<Server> leaders = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        if (leaders.isEmpty()) {
            log.info("===> elect for no leader, servers: {}", servers);
            elect(servers);
        } else if (leaders.size() > 1) {
            log.info("===> elect for multi leader, servers: {}", servers);
            elect(servers);
        } else {
            log.info("===> no need elect leader: {}", leaders.get(0));
        }
    }

    private void elect(List<Server> servers) {
        // 1. 各种节点自己选，算法保证大家选的是同一个
        // 2. 外部有一个分布式锁，谁拿到锁，谁当主
        // 3. 通过分布式一致性算法选举
        Server candidate = null;
        for (Server server : servers) {
            // 可能存在多个leader的情况，这里需要将所有的leader都设置为false
            // 保证选举后只有一个leader
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
            log.error(" ===>>> elect failed for no leaders");
        }
    }
}
