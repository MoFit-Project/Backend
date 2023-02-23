package Mofit.com.repository;

import Mofit.com.Domain.Room;

import java.util.List;
import java.util.Optional;


public interface RoomRepository {
    Optional<Room> findByRoomId(String roomId);
    List<Room> findAll();
}
