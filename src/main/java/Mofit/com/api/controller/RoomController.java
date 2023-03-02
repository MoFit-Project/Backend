package Mofit.com.api.controller;

import Mofit.com.Domain.Room;
import Mofit.com.api.request.CreateReq;
import Mofit.com.api.response.RoomRes;
import Mofit.com.api.request.LeaveRoomReq;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.api.service.RoomService;
import Mofit.com.exception.EntityNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    public RoomController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                          @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET, RoomService roomService) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
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

        List<RoomRes> rooms = new ArrayList<>();

        roomHashMap.keySet().forEach(roomName -> {
            RoomRes dto = new RoomRes();
            dto.setRoomId(roomName);
            dto.setParticipant(roomHashMap.get(roomName).getParticipant());
            rooms.add(dto);
        });

        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }


    @PostMapping("/leave/{roomId}")
    public ResponseEntity<String> leaveSessioin(@PathVariable String roomId, @RequestBody LeaveRoomReq leaveRoomReq)  {

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

                openVidu.getActiveSession(room.getRoomId()).close();

                return new ResponseEntity<>("deleteRoom", HttpStatus.OK);
            }
            return new ResponseEntity<>("존재하지 않는 방입니다", HttpStatus.NOT_IMPLEMENTED);
        }
        else{

            room.setParticipant(room.getParticipant()-1);
            roomHashMap.put(roomId, room);
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
        dto.setParticipant(1);

        roomHashMap.put(sessionId, dto);

            MakeRoomReq req = new MakeRoomReq();
     //        DB 저장..........
            req.setRoomId(sessionId);
            req.setRoomName(roomId);
            roomService.makeRoom(req);

        return new ResponseEntity<>(roomId,HttpStatus.OK);
    }

    @GetMapping("/enter/{sessionId}")
    public ResponseEntity<String> enterRoom(@PathVariable String sessionId) {

        Room room = roomService.findRoom(sessionId);
        if (room == null) {
            return new ResponseEntity<>(sessionId, HttpStatus.NOT_FOUND);

        }
        RoomRes dto = roomHashMap.get(room.getRoomId());
        //// 404 에러
        if(dto == null){
            return new ResponseEntity<>(room.getRoomId(), HttpStatus.NOT_FOUND);
        }
        if(dto.getParticipant() >= LIMIT){
            return new ResponseEntity<>("인원 초과", HttpStatus.BAD_REQUEST);
        }

        dto.setParticipant(dto.getParticipant()+1);
        roomHashMap.put(room.getRoomId(), dto);

        return new ResponseEntity<>(dto.getRoomId(), HttpStatus.OK);
    }

}
