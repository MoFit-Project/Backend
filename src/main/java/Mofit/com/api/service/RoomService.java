package Mofit.com.api.service;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Room;
import Mofit.com.api.request.CreateReq;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.request.RoomReq;
import Mofit.com.api.response.EnterRoomRes;
import Mofit.com.Domain.RoomData;
import Mofit.com.api.response.RoomRes;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RoomRepository;
import Mofit.com.util.RandomNumberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Service
@Transactional
public class RoomService {

    private static RoomRepository roomRepository = null;
    private final MemberRepository memberRepository;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int LIMIT = 2;

    private final ObjectMapper objectMapper;
    private static final WebClient webClient = WebClient.builder().baseUrl("https://ena.jegal.shop:8443").build();
    private static final String credentials = "OPENVIDUAPP:MY_SECRET";
    private static final String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));

    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    public RoomService(RoomRepository roomRepository,MemberRepository memberRepository,ObjectMapper objectMapper,
                       WebClient.Builder webClientBuilder) {

        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public RoomData getRoomRes(CreateReq request, String sessionId) {
        RoomData dto = new RoomData();

        dto.setUserId(request.getUserId());
        dto.setSessionId(sessionId);
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
    }

    public static Room findRoom(String roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        return room.orElse(null);
    }

    public static Mono<GameLeaveReq> endSignal(String roomId) {
        log.info("POST GAME END");

        Room roomData = findRoom(roomId);
        if (roomData == null) {
            throw new EntityNotFoundException("존재하지 않는 방입니다!");
        }
        RoomData room = (RoomData) roomData.getRes();
        GameLeaveReq dto = new GameLeaveReq();

        dto.setSession(room.getSessionId());
        dto.setType("end");
        dto.setData("End Game");

        return postMessage(dto, GameLeaveReq.class);
    }

    public static <T>Mono<T> postMessage(Object dto,Class<T> responseType) {
        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(responseType);

    }

    public ResponseEntity<EnterRoomRes> enterRoomBySession(String roomId, RoomReq request) {
        Room room = findRoom(roomId);
        EnterRoomRes enterRoom = new EnterRoomRes();
        // 404
        if (room == null) {
            return new ResponseEntity<>(enterRoom, HttpStatus.NOT_FOUND);
        }
        RoomData dto = room.getRes();
        //400
        if(dto.getParticipant() >= LIMIT){
            return new ResponseEntity<>(enterRoom, HttpStatus.BAD_REQUEST);
        }

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        dto.setParticipant(dto.getParticipant()+1);
        enterRoom.setSessionId(dto.getSessionId());
        enterRoom.setMode(dto.getMode());

        roomRepository.save(room);
//        roomHashMap.put(room.getRoomId(), dto);

        return new ResponseEntity<>(enterRoom, HttpStatus.OK);
    }
    public ResponseEntity<String> leave(String roomId, RoomReq leaveRoomReq,
                                        RoomData room)  {

        if(Objects.equals(room.getUserId(), leaveRoomReq.getUserId())) {
//            roomHashMap.remove(roomId);
            if(removeRoom(roomId)) {
                return new ResponseEntity<>("deleteRoom", HttpStatus.OK);
            }
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_IMPLEMENTED);
        }
        else{
            room.getGamers().remove(leaveRoomReq.getUserId());
            room.setParticipant(room.getParticipant()-1);

            if (room.getParticipant() >=1) {
                Room roomSave = Room.builder()
                        .roomId(roomId)
                        .res(room)
                        .build();
                roomRepository.save(roomSave);
//                roomHashMap.put(roomId, room);
            }
            else {
//                roomHashMap.remove(roomId);
                removeRoom(roomId);
            }
            return new ResponseEntity<>("leaveRoom", HttpStatus.OK);

        }
    }
    public ResponseEntity<String> createRoomBySession(String roomId, CreateReq request) {
        Room room = findRoom(roomId);

        if(room != null) {
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }
        Optional<Member> byAccount = memberRepository.findByAccount(request.getUserId());
        if(byAccount.isEmpty()){
            return new ResponseEntity<>("존재하지 않는 계정입니다", HttpStatus.NOT_FOUND);
        }

        String sessionId = RandomNumberUtil.getRandomNumber();
        RoomData dto = getRoomRes(request, sessionId);

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        Room roomSave = Room.builder()
                .roomId(roomId)
                .res(dto)
                .build();

        roomRepository.save(roomSave);
//        roomHashMap.put(sessionId, dto);

        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }

    private void setRoomList(List<RoomRes> rooms) {
        roomRepository.findAll().forEach(roomName -> {
            RoomData res = roomName.getRes();
            RoomRes dto = RoomRes.builder()
                    .createTime(res.getCreateTime())
                    .participant(res.getParticipant())
                    .RoomId(roomName.getRoomId())
                    .mode(res.getMode())
                    .build();
            rooms.add(dto);
        });

    }

    public JSONArray getRooms() throws ParseException, JsonProcessingException {
        List<RoomRes> rooms = new ArrayList<>();
        setRoomList(rooms);
        if (!rooms.isEmpty()){
            sortRoomRes(rooms);
        }
        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

}
