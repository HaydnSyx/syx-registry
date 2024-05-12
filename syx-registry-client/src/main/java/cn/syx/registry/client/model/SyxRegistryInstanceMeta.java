package cn.syx.registry.client.model;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"schema", "host", "port", "path"})
public class SyxRegistryInstanceMeta {

    private String schema;
    private String host;
    private int port;
    private String path;

    private boolean status;// online or offline
    private Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static SyxRegistryInstanceMeta http(String path) {
        String[] parts = path.split("_");
        return http(parts[0], Integer.parseInt(parts[1]));
    }

    public static SyxRegistryInstanceMeta http(String host, int port) {
        SyxRegistryInstanceMeta instance = new SyxRegistryInstanceMeta();
        instance.setSchema("http");
        instance.setHost(host);
        instance.setPort(port);
        instance.setPath("rpc");
        return instance;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, path);
    }

    public static SyxRegistryInstanceMeta from(String url) {
        URI uri = URI.create(url);
        SyxRegistryInstanceMeta instance = new SyxRegistryInstanceMeta();
        instance.setSchema(uri.getScheme());
        instance.setHost(uri.getHost());
        instance.setPort(uri.getPort());
        instance.setPath(uri.getPath().substring(1));
        return instance;
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
