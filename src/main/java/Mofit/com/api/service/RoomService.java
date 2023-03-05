package Mofit.com.api.service;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Room;
import Mofit.com.api.request.CreateReq;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.api.request.RoomReq;
import Mofit.com.api.response.EnterRoomRes;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
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

        return postMessage(dto, GameLeaveReq.class);
    }

    public static <T>Mono<T> postMessage(Object dto,Class<T> responseType) {
        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(60)); //타임 아웃 값을 60초로 설정
    }

    public ResponseEntity<EnterRoomRes> enterRoomBySession(String sessionId, RoomReq request,
                                                           ConcurrentMap<String, RoomRes> roomHashMap) {
        Room room = findRoom(sessionId);
        EnterRoomRes enterRoom = new EnterRoomRes();
        // 404
        if (room == null) {
            return new ResponseEntity<>(enterRoom, HttpStatus.NOT_FOUND);
        }
        RoomRes dto = roomHashMap.get(room.getRoomId());
        //404
        if(dto == null){
            return new ResponseEntity<>(enterRoom, HttpStatus.NOT_FOUND);
        }

        /// 체크
        enterRoom.setSessionId(dto.getRoomId());
        enterRoom.setMode(room.getMode());

        //400
        if(dto.getParticipant() >= LIMIT){
            return new ResponseEntity<>(enterRoom, HttpStatus.BAD_REQUEST);
        }

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        dto.setParticipant(dto.getParticipant()+1);
        roomHashMap.put(room.getRoomId(), dto);


        return new ResponseEntity<>(enterRoom, HttpStatus.OK);
    }
    public ResponseEntity<String> leave(String roomId, RoomReq leaveRoomReq,
                                        RoomRes room, ConcurrentMap<String, RoomRes> roomHashMap)  {

        if(Objects.equals(room.getUserId(), leaveRoomReq.getUserId())) {

            roomHashMap.remove(roomId);
            if(removeRoom(roomId)) {
                return new ResponseEntity<>("deleteRoom", HttpStatus.OK);
            }
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_IMPLEMENTED);
        }
        else{
            room.getGamers().remove(leaveRoomReq.getUserId());
            room.setParticipant(room.getParticipant()-1);

            if (room.getParticipant() >=1) {
                roomHashMap.put(roomId, room);
            }
            else {
                roomHashMap.remove(roomId);
                removeRoom(roomId);
            }
            return new ResponseEntity<>("leaveRoom", HttpStatus.OK);

        }
    }
    public ResponseEntity<String> createRoomBySession(String sessionId, CreateReq request,
                                                      ConcurrentMap<String,RoomRes> roomHashMap) {
        Room room = findRoom(sessionId);

        if(room != null) {
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }
        Optional<Member> byAccount = memberRepository.findByAccount(request.getUserId());
        if(byAccount.isEmpty()){
            return new ResponseEntity<>("존재하지 않는 계정입니다", HttpStatus.NOT_FOUND);
        }

        String roomId = RandomNumberUtil.getRandomNumber();
        RoomRes dto = getRoomRes(request, roomId);

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        roomHashMap.put(sessionId, dto);

        //    DB 저장..........
        MakeRoomReq req = new MakeRoomReq();
        req.setRoomId(sessionId);
        req.setRoomName(roomId);
        makeRoom(req);

        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }

    private void setRoomList(List<RoomRes> rooms,ConcurrentMap<String,RoomRes> roomHashMap) {
        roomHashMap.keySet().forEach(roomName -> {
            RoomRes dto = new RoomRes();
            dto.setRoomId(roomName);
            dto.setParticipant(roomHashMap.get(roomName).getParticipant());
            dto.setStatus(roomHashMap.get(roomName).getStatus());
            dto.setMode(roomHashMap.get(roomName).getMode());
            dto.setCreateTime(roomHashMap.get(roomName).getCreateTime());
            rooms.add(dto);
        });
    }

    public JSONArray getRooms(ConcurrentMap<String, RoomRes> roomHashMap) throws ParseException, JsonProcessingException {
        List<RoomRes> rooms = new ArrayList<>();
        setRoomList(rooms,roomHashMap);
        if (!rooms.isEmpty()){
            sortRoomRes(rooms);
        }
        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
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
