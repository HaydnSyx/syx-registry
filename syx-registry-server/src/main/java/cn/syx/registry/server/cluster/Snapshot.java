package cn.syx.registry.server.cluster;

import cn.syx.registry.core.model.RegistryInstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    private LinkedMultiValueMap<String, RegistryInstanceMeta> registry;
    private Map<String, Long> versions;
    private Map<String, Long> timestamps;
    private long version;
}
