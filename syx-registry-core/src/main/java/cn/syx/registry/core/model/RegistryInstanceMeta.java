package cn.syx.registry.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(of = {"schema", "host", "port", "path"})
public class RegistryInstanceMeta {

    private String schema;
    private String host;
    private int port;
    private String path;

    private boolean status;// online or offline
    private Map<String, String> parameters = new HashMap<>();

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, path);
    }

    public static RegistryInstanceMeta from(String url) {
        URI uri = URI.create(url);
        RegistryInstanceMeta instance = new RegistryInstanceMeta();
        instance.setSchema(uri.getScheme());
        instance.setHost(uri.getHost());
        instance.setPort(uri.getPort());
        instance.setPath(uri.getPath().substring(1));
        return instance;
    }
}
