package src.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class APIClient {
    private static final Logger logger = LoggerFactory.getLogger(APIClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode getJson(String urlString, Map<String, String> headers, int timeoutMs) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("GET");

            if (headers != null) {
                headers.forEach(conn::setRequestProperty);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("GET Request Failed. HTTP error code : " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return mapper.readTree(reader);
            }
        } catch (Exception e) {
            logger.error("GET request to {} failed", urlString, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static JsonNode postJson(String urlString, Map<String, String> headers, String jsonBody, int timeoutMs) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json");
            if (headers != null) {
                headers.forEach(conn::setRequestProperty);
            }

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != 200 && responseCode != 201) {
                throw new RuntimeException("POST Request Failed. HTTP error code : " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return mapper.readTree(reader);
            }
        } catch (Exception e) {
            logger.error("POST request to {} failed", urlString, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
