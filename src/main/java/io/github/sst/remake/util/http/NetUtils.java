package io.github.sst.remake.util.http;

import com.sun.net.httpserver.HttpExchange;
import io.github.sst.remake.Client;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetUtils {
    public static final String USER_AGENT = "SigmaRemake/" + Client.VERSION;

    public static CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create()
                .setUserAgent(USER_AGENT)
                .build();
    }

    public static InputStream getInputStreamFromURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to load image, HTTP response code: " + connection.getResponseCode());
        }

        return new BufferedInputStream(connection.getInputStream());
    }

    public static String getStringFromURL(String urlString) {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(urlString);
        httpGet.addHeader("ChatCommandExecutor-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");

        try (CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return entity == null ? "" : EntityUtils.toString(entity);
        } catch (IOException | ParseException e) {
            return "";
        }
    }

    public static URLConnection getConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(14000);
        connection.setReadTimeout(14000);
        connection.setUseCaches(true);

        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        return connection;
    }

    public static boolean isSafePath(String path, String prefix) {
        return path != null && path.startsWith(prefix) && !path.contains("..");
    }

    public static void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    public static void sendClasspathResource(HttpExchange exchange, String path, Class<?> resourceClass) throws IOException {
        try (InputStream stream = resourceClass.getResourceAsStream(path)) {
            if (stream == null) {
                sendNotFound(exchange);
                return;
            }

            byte[] response = IOUtils.toByteArray(stream);
            exchange.getResponseHeaders().add("Content-Type", getContentType(path));
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }

    public static String getContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".ttf")) {
            return "font/ttf";
        }
        if (lower.endsWith(".otf")) {
            return "font/otf";
        }
        if (lower.endsWith(".woff")) {
            return "font/woff";
        }
        if (lower.endsWith(".woff2")) {
            return "font/woff2";
        }
        if (lower.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (lower.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (lower.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        return "application/octet-stream";
    }
}
