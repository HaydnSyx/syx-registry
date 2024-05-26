package cn.syx.registry.core.model.instance;

import cn.syx.registry.core.model.IInstanceMeta;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"group", "namespace", "env", "name", "version"})
public class RpcServiceInstanceMeta implements IInstanceMeta {

    private String group;
    private String namespace;
    private String env;
    private String name;
    private String version;

    @Override
    public String identity() {
        return String.format("%s_%s_%s_%s_%s", namespace, env, group, name, version);
    }
}
