package cn.syx.registry.core.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public OkHttpInvoker(int connectionTimeout, int socketTimeout) {
        this.client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(16, 1, TimeUnit.MINUTES))
                .readTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public String post(String requestStr, String url) {
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
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
            throw new RuntimeException(e);
        }
    }
}
