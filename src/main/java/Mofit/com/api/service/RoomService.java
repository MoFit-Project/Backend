package Mofit.com.api.service;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.CreateReq;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper;
    private static final WebClient webClient = WebClient.builder().baseUrl("https://ena.jegal.shop:8443").build();
    private static final String credentials = "OPENVIDUAPP:MY_SECRET";
    private static final String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));

    @Autowired
    public RoomService(RoomRepository roomRepository,MemberRepository memberRepository,ObjectMapper objectMapper,
                       WebClient.Builder webClientBuilder) {

        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void makeRoom(MakeRoomReq makeRoomReq) {
        Room room = Room.builder()
                .roomId(makeRoomReq.getRoomId())
                .roomName(makeRoomReq.getRoomName())
                .mode("WAIT")
                .build();
        roomRepository.save(room);
    }
    public RoomRes getRoomRes(CreateReq request, String roomId) {
        RoomRes dto = new RoomRes();

        dto.setUserId(request.getUserId());
        dto.setRoomId(roomId);
        dto.setMode(request.getMode());
        dto.setParticipant(1);
        dto.setStatus("WAIT");
        dto.setTime(request.getTime());
        dto.setCreateTime(LocalDateTime.now().format(formatter));
        return dto;
    }

    public boolean removeRoom(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if(room.isPresent()){
            roomRepository.deleteById(roomId);
            return true;
        }
        return false;
    }
    public void sortRoomRes(List<RoomRes> room) {
        // createTime 필드를 기준으로 내림차순 정렬
        Comparator<RoomRes> comparator = Comparator.comparing(RoomRes::getCreateTime).reversed();

        // createTime이 같은 경우 roomId 기준으로 오름차순 정렬
        comparator = comparator.thenComparing(Comparator.comparing(RoomRes::getParticipant));

        room.sort(comparator);
        //return room;
    }



    public Room findRoom(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        return room.orElse(null);
    }


    public static RoomRes roomCheck(String roomId, ConcurrentMap<String, RoomRes> roomHashMap) {
        RoomRes room = roomHashMap.get(roomId);
        if (room == null) {
            throw new EntityNotFoundException(roomId);
        }
        room.setStatus("START");
        roomHashMap.put(roomId, room);
        return room;
    }
    public static Mono<GameLeaveReq> endSignal(String roomId, ConcurrentMap<String, RoomRes> roomHashMap) {
        log.info("POST GAME END");

        RoomRes room = roomCheck(roomId,roomHashMap);

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(room.getRoomId());
        dto.setType("end");
        dto.setData("End Game");

        return postMessage(dto);
    }

    public static Mono<GameLeaveReq> postMessage(GameLeaveReq dto) {
        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(GameLeaveReq.class);
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
