package Mofit.com.api.service;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.request.MakeRoomReq;

import Mofit.com.api.response.RoomRes;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    public RoomService(RoomRepository roomRepository,MemberRepository memberRepository,ObjectMapper objectMapper) {

        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
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
    public List<RoomRes> sortRoomRes(List<RoomRes> room) {
        objectMapper.registerModule(new JavaTimeModule());

        // createTime 필드를 기준으로 내림차순 정렬
        Comparator<RoomRes> comparator = Comparator.comparing(RoomRes::getCreateTime).reversed();

        // createTime이 같은 경우 roomId 기준으로 오름차순 정렬
        comparator = comparator.thenComparing(Comparator.comparing(RoomRes::getParticipant));

        room.sort(comparator);
        return room;
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
