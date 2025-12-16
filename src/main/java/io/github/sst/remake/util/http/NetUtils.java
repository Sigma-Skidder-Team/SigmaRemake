package io.github.sst.remake.util.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {

    public static InputStream getInputStreamFromURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to load image, HTTP response code: " + connection.getResponseCode());
        }

        return new BufferedInputStream(connection.getInputStream());
    }

}
