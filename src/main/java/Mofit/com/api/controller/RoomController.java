package Mofit.com.api.controller;

import Mofit.com.Domain.RoomDTO;
import Mofit.com.api.request.MakeRoomReq;
import Mofit.com.api.service.OpenviduService;
import Mofit.com.api.service.RoomService;
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
    private final OpenviduService openViduService;
    private final RoomService roomService;
    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    private Map<String , RoomDTO> roomHashMap = new ConcurrentHashMap<>();

    @Autowired
    public RoomController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                          @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET, OpenviduService openViduService,
                          RoomService roomService) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
        this.openVidu = new OpenVidu(OPENVIDU_URL,OPENVIDU_SECRET);
        this.openViduService = openViduService;
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
        for (String roomName : roomHashMap.keySet()) {
            roomHashMap.get(roomName).setRoomId(roomName);
            rooms.add(roomHashMap.get(roomName));
        }

        return (JSONArray) parser.parse(mapper.writeValueAsString(rooms));
    }

    // 전체 종료
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rooms/{sessionId}")
    public String leaveSessioin(@PathVariable String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {

        RoomDTO dto = roomHashMap.get(sessionId);

        Session session = openVidu.getActiveSession(dto.getRoomId());
        session.close();

        return "close";
    }


    @GetMapping("/create/{sessionId}")
    public ResponseEntity<String> createRoom(@PathVariable String sessionId) {

        boolean key = roomHashMap.containsKey(sessionId);
        if (key){
            return new ResponseEntity<>("이미 존재하는 방입니다", HttpStatus.FOUND);
        }
        String roomId = RandomNumberUtil.getRandomNumber();
        RoomDTO dto = new RoomDTO();

        dto.setRoomId(roomId);
        dto.setParticipant(1);

        roomHashMap.put(sessionId, dto);
//        MakeRoomReq req = new MakeRoomReq();


        // DB 저장..........
//        req.setRoomId(roomId);
//        req.setRoomName(sessionId);
//        roomService.makeRoom(req);

        return new ResponseEntity<>(roomId,HttpStatus.OK);
    }

    @GetMapping("/enter/{sessionId}")
    public ResponseEntity<RoomDTO> enterRoom(@PathVariable String sessionId) {
        boolean key = roomHashMap.containsKey(sessionId);

        RoomDTO roomDTO = roomHashMap.get(sessionId);
        if (key) {
            return new ResponseEntity<>(roomDTO, HttpStatus.OK);
        }

//        openVidu.fetch();
//        List<RoomDTO> room = openViduService.getRoom(openVidu.getActiveSessions());
//        for (RoomDTO roomDTO : room) {
//            if(roomDTO.getRoomId().equals(sessionId)){
//                return new ResponseEntity<>("OK", HttpStatus.OK);
//            }
//        }
        throw new RoomNotFoundException(sessionId);
    }

}
