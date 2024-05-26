package cn.syx.registry.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 注册服务实例
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
