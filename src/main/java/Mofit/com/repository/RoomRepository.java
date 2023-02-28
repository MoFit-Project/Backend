package Mofit.com.repository;

import Mofit.com.Domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoomRepository extends JpaRepository<Room,String> {
}
