package Mofit.com.api.controller;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Rank;
import Mofit.com.Domain.Room;
import Mofit.com.api.request.*;
import Mofit.com.api.response.EnterRoomRes;
import Mofit.com.api.response.RoomRes;
import Mofit.com.api.response.ResultRes;
import Mofit.com.api.service.RankingService;
import Mofit.com.api.service.RoomService;
import Mofit.com.repository.MemberRepository;
import Mofit.com.util.RandomNumberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RestController
@RequestMapping("/mofit")
public class RoomController {
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openVidu;
    private final RoomService roomService;
    private final RankingService rankService;
    private static final int LIMIT = 2;
    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentMap<String , RoomRes> roomHashMap = new ConcurrentHashMap<>();
    private static final int DELAY = 5;
    private final MemberRepository memberRepository;
    @Autowired
    public RoomController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                          @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET,RankingService rankService,
                          RoomService roomService,MemberRepository memberRepository) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
        this.memberRepository = memberRepository;
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

        return getRooms();
    }

    @GetMapping("/game/{roomId}")
    public Mono<GameLeaveReq> startSignal(@PathVariable String roomId) {
        log.info("POST GAME START");

        RoomRes room = RoomService.roomCheck(roomId,roomHashMap);

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(room.getRoomId());
        dto.setType("start");
        dto.setData("Let's Start");

        return RoomService.postMessage(dto, GameLeaveReq.class)
                .then(Mono.delay(Duration.ofSeconds(DELAY + room.getTime())))
                .then(RoomService.endSignal(roomId,roomHashMap));
    }
    @PostMapping("/game/{roomId}")
    public Mono<ResultRes> resultSignal(@PathVariable String roomId, @RequestBody ResultRes request) {

        RoomRes room = RoomService.roomCheck(roomId,roomHashMap);
        ResultRes dto = new ResultRes();

        dto.setSession(room.getRoomId());
        dto.setTo(request.getTo());
        dto.setType("result");
        // 담은 데이터 전송? 정렬 하면 된다
        dto.setData("Let's Start");

        return RoomService.postMessage(dto, ResultRes.class);
    }
    @PostMapping("/result/{roomId}")
    public ResponseEntity<String> gameResultMulti(@PathVariable String roomId,@RequestBody GameEndReq request){

        RoomRes roomRes = roomHashMap.get(roomId);
        if (roomRes == null) {
            return new ResponseEntity<>("존재하지 않는 유저", HttpStatus.BAD_REQUEST);
        }
        roomRes.getGamers().forEach(gamer -> rankService.updateRankWin(request.getUserId(),gamer));

        return new ResponseEntity<>("OK",HttpStatus.OK);
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
    public ResponseEntity<String> leaveSession(@PathVariable String roomId, @RequestBody RoomReq leaveRoomReq) throws OpenViduJavaClientException, OpenViduHttpException {

        RoomRes room = roomHashMap.get(roomId);
        if(room == null){
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }
        return leave(roomId, leaveRoomReq, room);

    }

    @PostMapping("/create/{sessionId}")
    public ResponseEntity<String> createRoom(@PathVariable String sessionId, @RequestBody CreateReq request) {

        return createRoomBySession(sessionId, request);
    }

    @PostMapping("/enter/{sessionId}")
    public ResponseEntity<EnterRoomRes> enterRoom(@PathVariable String sessionId, @RequestBody RoomReq request) {

        return enterRoomBySession(sessionId,request);
    }

    private ResponseEntity<EnterRoomRes> enterRoomBySession(String sessionId, RoomReq request) {
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

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        dto.setParticipant(dto.getParticipant()+1);
        roomHashMap.put(room.getRoomId(), dto);


        return new ResponseEntity<>(enterRoom, HttpStatus.OK);
    }
    private ResponseEntity<String> createRoomBySession(String sessionId, CreateReq request) {
        Room room = roomService.findRoom(sessionId);

        if(room != null) {
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }
        Optional<Member> byAccount = memberRepository.findByAccount(request.getUserId());
        if(byAccount.isEmpty()){
            return new ResponseEntity<>("존재하지 않는 계정입니다", HttpStatus.NOT_FOUND);
        }

        String roomId = RandomNumberUtil.getRandomNumber();
        RoomRes dto = roomService.getRoomRes(request, roomId);

        List<String> gamers = dto.getGamers();
        gamers.add(request.getUserId());

        roomHashMap.put(sessionId, dto);

        //    DB 저장..........
        MakeRoomReq req = new MakeRoomReq();
        req.setRoomId(sessionId);
        req.setRoomName(roomId);
        roomService.makeRoom(req);

        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
    private void setRoomList(List<RoomRes> rooms) {
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
    private ResponseEntity<String> leave(String roomId, RoomReq leaveRoomReq, RoomRes room)  {

        if(Objects.equals(room.getUserId(), leaveRoomReq.getUserId())) {

            roomHashMap.remove(roomId);
            if(roomService.removeRoom(roomId)) {
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
                roomService.removeRoom(roomId);
            }
            return new ResponseEntity<>("leaveRoom", HttpStatus.OK);

        }
    }

    private JSONArray getRooms() throws ParseException, JsonProcessingException {
        List<RoomRes> rooms = new ArrayList<>();
        setRoomList(rooms);
        if (!rooms.isEmpty()){
            roomService.sortRoomRes(rooms);
        }
        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }
}
