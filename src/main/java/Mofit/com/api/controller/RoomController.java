package Mofit.com.api.controller;

import Mofit.com.Domain.Room;
import Mofit.com.Domain.RoomDTO;
import Mofit.com.api.request.LeaveRoomReq;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.api.service.RoomService;
import Mofit.com.exception.custom.RoomNotAllowEnterException;
import Mofit.com.exception.custom.RoomNotFoundException;
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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/mofit")
public class RoomController {
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openVidu;
    private final RoomService roomService;
    private final int LIMIT = 4;
    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    private Map<String , RoomDTO> roomHashMap = new ConcurrentHashMap<>();

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

        List<RoomDTO> rooms = new ArrayList<>();

        roomHashMap.keySet().forEach(roomName -> {
            RoomDTO dto = new RoomDTO();
            dto.setRoomId(roomName);
            dto.setParticipant(roomHashMap.get(roomName).getParticipant());
            rooms.add(dto);
        });

        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }

    // 전체 종료
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/room/{sessionId}")
    public Boolean leaveSessioin(@PathVariable String sessionId,@RequestBody LeaveRoomReq leaveRoomReq) throws OpenViduJavaClientException, OpenViduHttpException {
        // SessionId -> dto에 저장된 변형된 roomId
        openVidu.fetch();

        roomHashMap.keySet().forEach(roomName-> leave(sessionId, leaveRoomReq, roomName));

        return true;
    }

    private void leave(String sessionId, LeaveRoomReq leaveRoomReq, String roomName) {
        if(roomHashMap.get(roomName).getRoomId().equals(sessionId)){
            RoomDTO dto = roomHashMap.get(roomName);
            if(!roomHashMap.containsKey(roomName)){
                throw new RoomNotFoundException(roomName);
            }
            if(leaveRoomReq.isHost()){
                hostLeaveRoom(sessionId, roomName, dto);
            }
            else{
                // 클라에서 처리?
                dto.setParticipant(dto.getParticipant() - 1);
                roomHashMap.put(roomName, dto);
            }
        }
    }

    private void hostLeaveRoom(String sessionId, String roomName, RoomDTO dto) {
        roomHashMap.remove(roomName);
        if(roomService.removeRoom(sessionId)) {
            Session session = openVidu.getActiveSession(dto.getRoomId());
            try {
                session.close();
            } catch (OpenViduJavaClientException e) {
                throw new RuntimeException(e);
            } catch (OpenViduHttpException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            throw new RoomNotFoundException(sessionId);
        }
    }

    @GetMapping("/create/{sessionId}")
    public ResponseEntity<String> createRoom(@PathVariable String sessionId) {

        Room room = roomService.findRoom(sessionId);
        if(room != null){
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }

        RoomDTO roomDto = roomHashMap.get(room.getRoomId());

        if (roomDto != null){
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }

        String roomId = RandomNumberUtil.getRandomNumber();
        RoomDTO dto = new RoomDTO();

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
            throw new RoomNotFoundException(sessionId);
        }
        RoomDTO dto = roomHashMap.get(room.getRoomId());
        //// 404 에러
        if(dto == null){
            throw new RoomNotFoundException(room.getRoomId());
        }
        if(dto.getParticipant() >= LIMIT){
            throw new RoomNotAllowEnterException(room.getRoomId());
        }

        dto.setParticipant(dto.getParticipant()+1);
        roomHashMap.put(room.getRoomId(), dto);

        return new ResponseEntity<>(room.getRoomId(), HttpStatus.OK);
    }

}
