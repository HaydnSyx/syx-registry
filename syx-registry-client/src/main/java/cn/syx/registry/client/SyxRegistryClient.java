package cn.syx.registry.client;

import cn.syx.registry.client.config.SyxRegistryClientProperties;
import cn.syx.registry.core.model.RegistryInstanceMeta;
import cn.syx.registry.core.model.Server;
import cn.syx.registry.core.util.OkHttpClientHelper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SyxRegistryClient {

    private static final String TYPE_REG = "reg";
    private static final String TYPE_UNREG = "unreg";
    private static final String TYPE_FETCH_ALL = "fetch_all";
    private static final String TYPE_RENEWS = "renews";

    private static final String TYPE_VERSION = "version";

    private static final String TYPE_LEADER_INFO = "leader_info";

    private final OkHttpClient client;

    private SyxRegistryClientProperties properties;

    private List<String> servers;
    private String leaderServer;
    private AtomicInteger index = new AtomicInteger(0);

    private ScheduledExecutorService heartbeatExecutorService;

    MultiValueMap<RegistryInstanceMeta, String> RENEWS = new LinkedMultiValueMap<>();

    public SyxRegistryClient(SyxRegistryClientProperties properties) {
        this.properties = properties;
        String[] serversArr = properties.getServers().split(",");
        if (serversArr.length == 0) {
            throw new RuntimeException("registry center servers is empty");
        }

        this.servers = Arrays.stream(serversArr).collect(Collectors.toList());

        this.client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(properties.getMaxConnections(), properties.getKeepLiveSec(), TimeUnit.SECONDS))
                .readTimeout(properties.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(properties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    public void start() {
        log.debug("===> SyxRegistryClient started.");
        this.leaderServer = leaderInfo();
        heartbeatExecutorService = Executors.newScheduledThreadPool(1);
        heartbeatExecutorService.scheduleWithFixedDelay(() -> {
            RENEWS.keySet().forEach(e -> {
                String services = String.join(",", RENEWS.get(e).toArray(String[]::new));
                log.debug("===> SyxRegistryClient renew instance: {} for services: {}", e, services);
                OkHttpClientHelper.httpPost(client, JSON.toJSONString(e), renewsPath() + services, Long.class);
            });
        }, properties.getInitDelayCheckTime(), properties.getDelayCheckGapTime(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        log.debug("===> SyxRegistryClient stopped.");
        gracefulShutdown(heartbeatExecutorService);
    }

    private void gracefulShutdown(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("===> SyxRegistryClient stop error", e);
        }
    }

    public String leaderInfo() {
        log.debug("===> SyxRegistryClient get leader debug");
        Server server = OkHttpClientHelper.httpGet(client, leaderInfoPath(), Server.class);
        log.debug("===> SyxRegistryClient get leader debug end result: {}", server);
        return server.getUrl();
    }

    public void register(String servicePath, RegistryInstanceMeta instance) {
        log.debug("===> SyxRegistryClient registry start instance: {} for service: {}", instance, servicePath);
        OkHttpClientHelper.httpPost(client, JSON.toJSONString(instance), registerPath() + servicePath, RegistryInstanceMeta.class);
        log.debug("===> SyxRegistryClient registry end instance: {}", instance);
        RENEWS.add(instance, servicePath);
    }

    public void unregister(String servicePath, RegistryInstanceMeta instance) {
        log.debug("===> SyxRegistryClient unregistry start instance: {} for service: {}", instance, servicePath);
        OkHttpClientHelper.httpPost(client, JSON.toJSONString(instance), unregisterPath() + servicePath, RegistryInstanceMeta.class);
        log.debug("===> SyxRegistryClient unregistry end instance: {}", instance);
    }

    public Long version(String servicePath) {
        log.debug("===> SyxRegistryClient version start service: {}", servicePath);
        Long version = OkHttpClientHelper.httpGet(client, versionPath() + servicePath, Long.class);
        log.debug("===> SyxRegistryClient version end service: {}, version: {}", servicePath, version);
        return version;
    }

    public List<RegistryInstanceMeta> fetchAll(String servicePath) {
        log.debug("===> SyxRegistryClient fetchAll start service: {}", servicePath);
        List<RegistryInstanceMeta> instanceMetas = OkHttpClientHelper.httpGet(client, fetchAllPath() + servicePath, new TypeReference<List<RegistryInstanceMeta>>() {
        });
        log.debug("===> SyxRegistryClient fetchAll end service: {}, instances: {}", servicePath, instanceMetas);
        return instanceMetas;
    }

    private String registerPath() {
        return getRegistryCenterUrl(TYPE_REG) + "/reg?service=";
    }

    private String unregisterPath() {
        return getRegistryCenterUrl(TYPE_UNREG) + "/unreg?service=";
    }

    private String fetchAllPath() {
        return getRegistryCenterUrl(TYPE_FETCH_ALL) + "/findAll?service=";
    }

    private String renewsPath() {
        return getRegistryCenterUrl(TYPE_RENEWS) + "/renews?services=";
    }

    private String leaderInfoPath() {
        return getRegistryCenterUrl(TYPE_LEADER_INFO) + "/leader";
    }

    private String versionPath() {
        return getRegistryCenterUrl(TYPE_VERSION) + "/version?service=";
    }

    private String getRegistryCenterUrl(String type) {
        if (servers.size() == 1) {
            return servers.get(0);
        }

        if (Objects.equals(TYPE_FETCH_ALL, type) || Objects.equals(TYPE_LEADER_INFO, type)
                || Objects.equals(TYPE_VERSION, type)) {
            // 轮询获取一个节点
            return servers.get((index.getAndIncrement() & Integer.MAX_VALUE) % servers.size());
        }

        // 获取主节点
        if (Objects.isNull(leaderServer)) {
            this.leaderServer = leaderInfo();
        }
        return leaderServer;
    }
}
