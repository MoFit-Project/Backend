package Mofit.com.api.service;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.request.MakeRoomReq;

import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository,MemberRepository memberRepository) {

        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
    }

    public void makeRoom(MakeRoomReq makeRoomReq) {
        Room room = Room.builder()
                .roomId(makeRoomReq.getRoomId())
                .roomName(makeRoomReq.getRoomName())
                .mode("WAIT")
                .build();
        roomRepository.save(room);
    }

    public boolean removeRoom(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if(room.isPresent()){
            roomRepository.deleteById(roomId);
            return true;
        }
        return false;
    }



    public Room findRoom(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        return room.orElse(null);
    }

    public void updateMode(String roomId,String mode) {
        Room updateRoom = roomRepository.findById(roomId).orElse(null);

        if (updateRoom == null) {
            throw new EntityNotFoundException(roomId);
        }
        updateRoom.setMode(mode);
        roomRepository.save(updateRoom);
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

}
