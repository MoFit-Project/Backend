package Mofit.com.api.controller;


import Mofit.com.Domain.Room;
import Mofit.com.api.request.*;
import Mofit.com.api.response.EnterRoomRes;
import Mofit.com.Domain.RoomData;
import Mofit.com.api.response.ResultRes;
import Mofit.com.api.service.RankingService;
import Mofit.com.api.service.RoomService;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.openvidu.java.client.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/mofit")
public class RoomController {
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openVidu;
    private final RoomService roomService;
    private final RankingService rankService;
    private static final long DELAY = 5L;

    @Autowired
    public RoomController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                          @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET,RankingService rankService,
                          RoomService roomService,MemberRepository memberRepository) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
        this.openVidu = new OpenVidu(OPENVIDU_URL,OPENVIDU_SECRET);
        this.roomService = roomService;
        this.rankService = rankService;

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

        return roomService.getRooms();
    }

    @GetMapping("/game/{roomId}")
    @Async
    public CompletableFuture<GameLeaveReq> startSignal(@PathVariable String roomId) {
        log.info("POST GAME START");


        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            throw new EntityNotFoundException("존재하지 않는 방입니다!");
        }
        RoomData roomRes = roomData.getRes();

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(roomRes.getSessionId());
        dto.setType("start");
        dto.setData("Let's Start");

        long delaySeconds = DELAY + roomRes.getTime();
        return RoomService.postMessage(dto, GameLeaveReq.class)
                .then(Mono.delay(Duration.ofSeconds(delaySeconds)))
                .then(RoomService.endSignal(roomId).subscribeOn(Schedulers.boundedElastic()))
                .toFuture()
                .exceptionally(ex -> {
                    log.error("Error occurred: " + ex.getMessage());
                    return null;
                });
    }
    @PostMapping("/game/{roomId}")
    public Mono<ResultRes> resultSignal(@PathVariable String roomId, @RequestBody ResultRes request) {

        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            throw new EntityNotFoundException("존재하지 않는 방입니다!");
        }
        RoomData roomRes = roomData.getRes();

        ResultRes dto = new ResultRes();

        dto.setSession(roomRes.getSessionId());
        dto.setTo(request.getTo());
        dto.setType("result");
        dto.setData("Game End");

        return RoomService.postMessage(dto, ResultRes.class);
    }
    @PostMapping("/result/{roomId}")
    public ResponseEntity<String> gameResultMulti(@PathVariable String roomId,@RequestBody GameEndReq request){

        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            return new ResponseEntity<>("존재하지 방입니다", HttpStatus.BAD_REQUEST);
        }

        RoomData room = roomData.getRes();
        if (room == null) {
            return new ResponseEntity<>("존재하지 않는 유저", HttpStatus.BAD_REQUEST);
        }
        room.getGamers().forEach(gamer -> rankService.updateRankWin(request.getUserId(),gamer));

        return new ResponseEntity<>("OK",HttpStatus.OK);
    }


    @GetMapping("/destroy/{roomId}")
    public ResponseEntity<String> destroySession(@PathVariable String roomId) throws OpenViduJavaClientException, OpenViduHttpException {
//        RoomRes room = roomHashMap.get(roomId);
        Room roomData = RoomService.findRoom(roomId);
        if(roomData == null){
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }

        RoomData room = roomData.getRes();
        roomService.removeRoom(roomId);

        if(room == null){
            return new ResponseEntity<>("삭제완료", HttpStatus.OK);
        }

        openVidu.getActiveSession(room.getSessionId()).close();

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/leave/{roomId}")
    public ResponseEntity<String> leaveSession(@PathVariable String roomId, @RequestBody RoomReq leaveRoomReq) {


        Room roomData = RoomService.findRoom(roomId);
        if(roomData == null){
            return new ResponseEntity<>("이미 나갔습니다", HttpStatus.NOT_FOUND);
        }

        RoomData room = roomData.getRes();

        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        log.info("roomId = {}",roomId);
//        RoomRes room = roomHashMap.get(roomId);
        if(room == null){
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }
        return roomService.leave(roomId, leaveRoomReq, room);

    }

    @PostMapping("/create/{sessionId}")
    public ResponseEntity<String> createRoom(@PathVariable String sessionId, @RequestBody CreateReq request) {

        return roomService.createRoomBySession(sessionId, request);
    }

    @PostMapping("/enter/{sessionId}")
    public ResponseEntity<EnterRoomRes> enterRoom(@PathVariable String sessionId, @RequestBody RoomReq request) {

        return roomService.enterRoomBySession(sessionId,request);
    }


}
