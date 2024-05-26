package cn.syx.registry.core.model.instance;

import cn.syx.registry.core.model.IMeta;
import lombok.*;

/**
 * RPC服务元数据
 * <p>
 * env + namespace 区分环境
 * <p>
 * group + name + version 唯一标识一个服务
 *
 * @author syx
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"env", "namespace", "group", "name", "version"})
public class RpcServiceMeta implements IMeta {

    /** 环境:开发\预发\线上 */
    private String env;
    /** 空间 */
    private String namespace;

    /** 服务所属组 */
    private String group;
    /** 服务类名 */
    private String name;
    /** 服务版本 */
    private String version;

    @Override
    public String identity() {
        return String.format("%s_%s_%s_%s_%s", env, namespace, group, name, version);
    }
}
