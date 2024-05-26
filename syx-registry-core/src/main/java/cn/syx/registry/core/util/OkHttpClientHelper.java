package cn.syx.registry.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;

@Slf4j
public class OkHttpClientHelper {

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public static <T> T httpGet(OkHttpClient client, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = get(client, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    public static <T> T httpGet(OkHttpClient client, String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpGet: " + url);
        String data = get(client, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }


    public static <T> T httpPost(OkHttpClient client, String requestStr, String url, Class<T> clazz) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = post(client, requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, clazz);
    }

    public static <T> T httpPost(OkHttpClient client, String requestStr, String url, TypeReference<T> ref) {
        log.debug(" =====>>>>>> httpPost: " + url);
        String data = post(client, requestStr, url);
        log.debug(" =====>>>>>> response: " + data);
        return JSON.parseObject(data, ref);
    }

    private static String get(OkHttpClient client, String url) {
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(req).execute()) {
            ResponseBody responseBody = response.body();
            if (Objects.isNull(responseBody)) {
                throw new RuntimeException("provider response is null");
            }

            String data = responseBody.string();
            log.debug("provider response ======> {}", data);
            return data;
        } catch (Exception e) {
            log.error("syx registry client http get error", e);
            throw new RuntimeException(e);
        }
    }

    private static String post(OkHttpClient client, String requestStr, String url) {
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestStr, mediaType))
                .build();
        try (Response response = client.newCall(req).execute()) {
            ResponseBody responseBody = response.body();
            if (Objects.isNull(responseBody)) {
                throw new RuntimeException("provider response is null");
            }

            String data = responseBody.string();
            log.debug("provider response ======> {}", data);
            return data;
        } catch (Exception e) {
            log.error("syx registry client http post error", e);
            throw new RuntimeException(e);
        }
    }
}
