package com.otodosov.gitter;


import com.otodosov.gitter.recources.JoinRoom;
import com.otodosov.gitter.recources.Message;
import com.otodosov.gitter.recources.Room;
import com.otodosov.gitter.recources.UpdateRoom;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GitterTest {

    private final String ACCESS_TOKEN = "";

    private ApiService apiService = new ApiService(ACCESS_TOKEN);

    @Test
    public void testRoomsExist() throws Exception {
        Room[] rooms = apiService.getRooms();

        assertEquals(3, rooms.length);
//        assertThat(Stream.of(rooms).map(Room::getName).collect(toList()), hasItems("otodosovTest/testRoom1", "otodosovTest/testRoom2", "otodosovTest/testRoom3"));
    }

    @Test
    public void testChangeRoomTopic() throws Exception {
        //get one of current rooms
        String roomId = apiService.getRooms()[0].getId();

        //set up new topic
        String newTopic = String.valueOf(new Random().nextInt(100000000));
        UpdateRoom newRoom = UpdateRoom.builder().topic(newTopic).build();

        //update topic
        apiService.updateRoomById(roomId, newRoom);

        //check updated room
        Room updatedRoom = apiService.getRoomById(roomId);
        assertEquals(newTopic, updatedRoom.getTopic());
    }

    @Test
    public void testLeaveAndJoinRoom() throws Exception {
        //get current user
        String userId = apiService.getCurrentUser().getId();

        //get room of current user
        String roomId = apiService.getRoomsByUser(userId)[0].getId();

        //leave room and check
        apiService.leaveRoomForUser(roomId, userId);
        Room[] rooms = apiService.getRoomsByUser(userId);
        assertThat(Stream.of(rooms).map(Room::getId).collect(toList()), not(hasItem(roomId)));

        //join room and check
        apiService.joinRoomForUser(userId, JoinRoom.builder().id(roomId).build());
        rooms = apiService.getRoomsByUser(userId);
        assertThat(Stream.of(rooms).map(Room::getId).collect(toList()), hasItem(roomId));
    }

    @Test(timeout = 60000L)
    public void testMessagingViaStreamApi() throws Exception {
        apiService = new ApiService(ACCESS_TOKEN);
        String roomId = apiService.getRooms()[0].getId();

        StreamApiExecutor executor = new StreamApiExecutor(roomId, ACCESS_TOKEN, this);
        Thread executorThread = new Thread(executor, "gitterStreamApiExecutor");
        executorThread.start();

        //wait till stream api executor set up connection
        synchronized (this) {
            wait();
        }

        for (int i = 0; i < 3; i++) {
            Message message = Message.builder().text("test message " + i).build();
            apiService.sendMessage(roomId, message);
        }

        int messageCount = 0;
        while (messageCount != 3) {
            List<Message> responses = executor.getResponses();
            if (responses.size() > messageCount) {
                Message message = responses.get(messageCount);
                assertEquals("test message " + messageCount, message.getText());

                messageCount++;
            }
        }

        executor.stop();
        executorThread.join();
    }
}
