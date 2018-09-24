package com.otodosov.gitter;

import com.otodosov.gitter.recources.JoinRoom;
import com.otodosov.gitter.recources.Message;
import com.otodosov.gitter.recources.User;
import com.otodosov.gitter.recources.Room;
import com.otodosov.gitter.recources.UpdateRoom;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiService {

    private final String GITTER_API = "https://api.gitter.im/v1";

    private String accessToken;
    private RestTemplate restTemplate;

    public ApiService(String accessToken) {
        this.restTemplate = new RestTemplate();
        this.accessToken = accessToken;
    }

    public Room[] getRooms() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("rooms")
                .queryParam("access_token", accessToken);

        return restTemplate.getForObject(uriBuilder.build().toUri(), Room[].class);
    }

    public Room getRoomById(String id) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("rooms", id)
                .queryParam("access_token", accessToken);

        return restTemplate.getForObject(uriBuilder.build().toUri(), Room.class);
    }

    public Room updateRoomById(String id, UpdateRoom updateRoom) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("rooms", id)
                .queryParam("access_token", accessToken);

        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.PUT, new HttpEntity<>(updateRoom), Room.class).getBody();
    }

    public User getCurrentUser() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("user")
                .queryParam("access_token", accessToken);

        return restTemplate.getForObject(uriBuilder.build().toUri(), User[].class)[0];
    }

    public void leaveRoomForUser(String roomId, String userId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("rooms", roomId, "users", userId)
                .queryParam("access_token", accessToken);

        restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.DELETE, new HttpEntity<>(""), String.class);
    }

    public void joinRoomForUser(String userId, JoinRoom joinRoom) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("user", userId, "rooms")
                .queryParam("access_token", accessToken);

        restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.POST, new HttpEntity<>(joinRoom), String.class);
    }

    public Room[] getRoomsByUser(String userId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("user", userId, "rooms")
                .queryParam("access_token", accessToken);
        return restTemplate.getForObject(uriBuilder.build().toUri(), Room[].class);
    }

    public void sendMessage(String roomId, Message message) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GITTER_API)
                .pathSegment("rooms", roomId, "chatMessages")
                .queryParam("access_token", accessToken);

        restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.POST, new HttpEntity<>(message), String.class);
    }
}
