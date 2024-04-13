package cn.syx.registry.core.model;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {

    private String schema;
    private String host;
    private int port;
    private String context;

    private boolean status;// online or offline
    private Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String path) {
        String[] parts = path.split("_");
        return http(parts[0], Integer.parseInt(parts[1]));
    }

    public static InstanceMeta http(String host, int port) {
        InstanceMeta instance = new InstanceMeta();
        instance.setSchema("http");
        instance.setHost(host);
        instance.setPort(port);
        instance.setContext("rpc");
        return instance;
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        InstanceMeta instance = new InstanceMeta();
        instance.setSchema(uri.getScheme());
        instance.setHost(uri.getHost());
        instance.setPort(uri.getPort());
        instance.setContext(uri.getPath().substring(1));
        return instance;
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
