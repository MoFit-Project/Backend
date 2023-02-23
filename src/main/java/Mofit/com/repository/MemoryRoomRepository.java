package Mofit.com.repository;

import Mofit.com.Domain.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryRoomRepository implements RoomRepository{
    private static Map<String, Room> store = new ConcurrentHashMap<>();
    @Override
    public Optional<Room> findByRoomId(String roomId) {
        return Optional.ofNullable(store.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(store.values());
    }
}
