package io.github.sst.remake.util.http;

import io.github.sst.remake.Client;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
