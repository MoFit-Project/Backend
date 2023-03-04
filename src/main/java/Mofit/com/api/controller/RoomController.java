package Mofit.com.api.controller;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.CreateReq;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.response.EnterRoomRes;
import Mofit.com.api.response.RoomRes;
import Mofit.com.api.request.LeaveRoomReq;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.api.service.RoomService;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.util.RandomNumberUtil;
import Mofit.com.util.RoomsComparator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/mofit")
public class RoomController {
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openVidu;
    private final RoomService roomService;
    private static final int LIMIT = 2;
    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    private Map<String , RoomRes> roomHashMap = new ConcurrentHashMap<>();

    private final WebClient webClient;

    String credentials = "OPENVIDUAPP:MY_SECRET";
    String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));


    @Autowired
    public RoomController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                          @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET, RoomService roomService, WebClient.Builder webClientBuilder) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
        this.webClient = webClientBuilder.baseUrl("https://ena.jegal.shop:8443").build();
        this.openVidu = new OpenVidu(OPENVIDU_URL,OPENVIDU_SECRET);
        this.roomService = roomService;

    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/sessions")
    public String initialSession(@RequestBody(required = false) Map<String,Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {

        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openVidu.createSession(properties);

        return session.getSessionId();
    }
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/sessions/{sessionId}/connections")
    public String createConnection(@PathVariable String sessionId, @RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);
        if (session == null) {
            return "No";
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);

        return connection.getToken();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/rooms")
    public JSONArray findSessions()
            throws JsonProcessingException, ParseException {

        return getRooms();
    }

    private JSONArray getRooms() throws ParseException, JsonProcessingException {
        List<RoomRes> rooms = new ArrayList<>();

        roomHashMap.keySet().forEach(roomName -> {
            RoomRes dto = new RoomRes();
            dto.setRoomId(roomName);
            dto.setParticipant(roomHashMap.get(roomName).getParticipant());
            dto.setStatus(roomHashMap.get(roomName).getStatus());
            dto.setMode(roomHashMap.get(roomName).getMode());
            dto.setCreateTime(roomHashMap.get(roomName).getCreateTime());
            rooms.add(dto);
        });
        if (!rooms.isEmpty()){
            rooms.sort(new RoomsComparator());
        }
        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }

    @GetMapping("/game/{roomId}")
    public Mono<GameLeaveReq> startSignal(@PathVariable String roomId) {
        log.info("POST GAME START");

        RoomRes room = roomCheck(roomId);

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(room.getRoomId());
        dto.setType("start");
        dto.setData("Let's Start");


        return postMessage(dto)
                .then(Mono.delay(Duration.ofSeconds(room.getTime()+3)))
                .then(endSignal(roomId));
    }

    private RoomRes roomCheck(String roomId) {
        RoomRes room = roomHashMap.get(roomId);
        if (room == null) {
            throw new EntityNotFoundException(roomId);
        }

        room.setStatus("START");
        roomHashMap.put(roomId, room);
        return room;
    }


    public Mono<GameLeaveReq> endSignal(String roomId) {
        log.info("POST GAME END");

        RoomRes room = roomCheck(roomId);

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(room.getRoomId());

        dto.setType("end");
        dto.setData("End Game");

        return postMessage(dto);
    }

    private Mono<GameLeaveReq> postMessage(GameLeaveReq dto) {
        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(GameLeaveReq.class);
    }


    @GetMapping("/destroy/{roomId}")
    public ResponseEntity<String> destroySession(@PathVariable String roomId) throws OpenViduJavaClientException, OpenViduHttpException {
        RoomRes room = roomHashMap.get(roomId);
        if(room == null){
            return new ResponseEntity<>("삭제완료", HttpStatus.OK);
        }

        openVidu.getActiveSession(room.getRoomId()).close();

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }




    @PostMapping("/leave/{roomId}")
    public ResponseEntity<String> leaveSession(@PathVariable String roomId, @RequestBody LeaveRoomReq leaveRoomReq) throws OpenViduJavaClientException, OpenViduHttpException {

        RoomRes room = roomHashMap.get(roomId);
        if(room == null){
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }
        return leave(roomId, leaveRoomReq, room);

    }

    private ResponseEntity<String> leave(String roomId, LeaveRoomReq leaveRoomReq, RoomRes room) throws OpenViduJavaClientException, OpenViduHttpException {

        if(Objects.equals(room.getUserId(), leaveRoomReq.getUserId())){

            roomHashMap.remove(roomId);
            if(roomService.removeRoom(roomId)){
                return new ResponseEntity<>("deleteRoom", HttpStatus.OK);
            }
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_IMPLEMENTED);
        }
        else{

            room.setParticipant(room.getParticipant()-1);
            if (room.getParticipant() >=1){
                roomHashMap.put(roomId, room);
            }
            else{
                roomHashMap.remove(roomId);
                roomService.removeRoom(roomId);
            }
            return new ResponseEntity<>("leaveRoom", HttpStatus.OK);

        }
    }



    @PostMapping("/create/{sessionId}")
    public ResponseEntity<String> createRoom(@PathVariable String sessionId, @RequestBody CreateReq request) {

        Room room = roomService.findRoom(sessionId);
        if(room != null){
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }

        String roomId = RandomNumberUtil.getRandomNumber();
        RoomRes dto = new RoomRes();

        log.info("user id = {}",request.getUserId());
        dto.setUserId(request.getUserId());
        dto.setRoomId(roomId);
        dto.setMode(request.getMode());
        dto.setParticipant(1);
        dto.setStatus("WAIT");
        dto.setTime(request.getTime());
        dto.setCreateTime(LocalDateTime.now());

        roomHashMap.put(sessionId, dto);

            MakeRoomReq req = new MakeRoomReq();
     //        DB 저장..........
            req.setRoomId(sessionId);
            req.setRoomName(roomId);
            roomService.makeRoom(req);

        return new ResponseEntity<>(roomId,HttpStatus.OK);
    }

    @GetMapping("/enter/{sessionId}")
    public ResponseEntity<EnterRoomRes> enterRoom(@PathVariable String sessionId) {


        Room room = roomService.findRoom(sessionId);
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

        dto.setParticipant(dto.getParticipant()+1);
        roomHashMap.put(room.getRoomId(), dto);

        return new ResponseEntity<>(enterRoom, HttpStatus.OK);
    }

}
