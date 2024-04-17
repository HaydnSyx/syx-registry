package cn.syx.registry.core.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker DEFAULT = new OkHttpInvoker(500, 500);

    String post(String requestStr, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = DEFAULT.get(url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = DEFAULT.get(url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }


    static <T> T httpPost(String requestStr, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = DEFAULT.post(requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    static <T> T httpPost(String requestStr, String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = DEFAULT.post(requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }
}
