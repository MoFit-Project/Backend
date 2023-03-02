package Mofit.com.api.service;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.request.MakeRoomReq;

import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    String credentials = "OPENVIDUAPP:MY_SECRET";
    String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));
    private final WebClient webClient;
    @Autowired
    public RoomService(RoomRepository roomRepository,MemberRepository memberRepository,WebClient.Builder webClientBuilder) {

        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.webClient =  webClientBuilder.baseUrl("https://ena.jegal.shop:8443").build();
    }


    public void makeRoom(MakeRoomReq makeRoomReq) {
        Room room = Room.builder()
                .roomId(makeRoomReq.getRoomId())
                .roomName(makeRoomReq.getRoomName())
                .status("WAIT")
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

    public void updateStatus(String roomId,String status) {
        Room updateRoom = roomRepository.findById(roomId).orElse(null);

        if (updateRoom == null) {
            throw new EntityNotFoundException(roomId);
        }
        updateRoom.setStatus(status);
        roomRepository.save(updateRoom);
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }



    public Mono<GameLeaveReq> leaveSignal(GameLeaveReq request) {

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(request.getSession());
        dto.setTo(request.getTo());
        dto.setType("leaveSession");
        dto.setData("LeaveSession");

        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(GameLeaveReq.class);
    }
}
