package com.otodosov.gitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otodosov.gitter.recources.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class StreamApiExecutor implements Runnable {

    private final String API_URL = "https://stream.gitter.im/v1/rooms/%s/chatMessages";

    private volatile boolean stop;

    private String roomId;

    @Getter
    private List<Message> responses;

    private ObjectMapper objectMapper;

    private Object parent;

    private String accessToken;

    public StreamApiExecutor(String roomId, String accessToken, Object parent) {
        this.roomId = roomId;
        this.stop = false;
        this.objectMapper = new ObjectMapper();
        this.responses = Collections.synchronizedList(new ArrayList<>());
        this.parent = parent;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        log.info("start stream api thread");
        HttpURLConnection urlConnection = null;
        try {
            String url = String.format(API_URL, roomId);
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod(String.valueOf(HttpMethod.GET));
            urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);

            urlConnection.connect();
            synchronized (parent) {
                parent.notify();
            }

            log.info("connecting to {}", url);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                while (!stop) {
                    log.info("waiting for response...");
                    String messageResponse = in.readLine();
                    log.info("received message {}", messageResponse);
                    if (!messageResponse.trim().isEmpty()) {
                        Message message = objectMapper.readValue(messageResponse, Message.class);
                        log.info("converted message {}", message);
                        responses.add(message);
                    }
                }
                log.info("closing connection...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

    }

    public void stop() {
        this.stop = true;
    }
}
