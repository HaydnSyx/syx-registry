package cn.syx.registry.core.cluster;

import cn.syx.registry.core.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    private LinkedMultiValueMap<String, InstanceMeta> registry;
    private Map<String, Long> versions;
    private Map<String, Long> timestamps;
    private long version;
}
