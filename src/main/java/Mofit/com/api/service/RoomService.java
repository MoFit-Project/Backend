package Mofit.com.api.service;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.FindRoomReq;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.exception.custom.RoomNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
                .status("WAIT")
                .build();
        roomRepository.save(room);
    }

    public Room findRoom(FindRoomReq findRoomReq) {
        Optional<Room> room = roomRepository.findById(findRoomReq.getRoomId());
        if(room.isPresent()){

            return room.get();
        }
        throw new RoomNotFoundException(findRoomReq.getRoomId());
    }
    public void updateStatus(String roomId,String status) {
        Room updateRoom = roomRepository.findById(roomId).orElse(null);

        if (updateRoom == null) {
            throw new RoomNotFoundException(roomId);
        }
        updateRoom.setStatus(status);
        roomRepository.save(updateRoom);
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

}
