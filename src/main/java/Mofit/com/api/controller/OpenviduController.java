package Mofit.com.api.controller;

import Mofit.com.api.service.OpenviduService;
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
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/mofit")
public class OpenviduController {
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openVidu;
    private final OpenviduService openViduService;
    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    public OpenviduController(@Value("${OPENVIDU_URL}") String OPENVIDU_URL,
                              @Value("${OPENVIDU_SECRET}") String OPENVIDU_SECRET,OpenviduService openViduService) {
        this.OPENVIDU_URL = OPENVIDU_URL;
        this.OPENVIDU_SECRET = OPENVIDU_SECRET;
        this.openVidu = new OpenVidu(OPENVIDU_URL,OPENVIDU_SECRET);
        this.openViduService = openViduService;
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
            throws OpenViduJavaClientException, OpenViduHttpException, JsonProcessingException, ParseException {

        openVidu.fetch();
        return (JSONArray) parser.parse(mapper.writeValueAsString(openViduService.getRoomMap(openVidu.getActiveSessions())));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/rooms/{sessionId}")
    public String leaveSessioin(@PathVariable String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {

        Session session = openVidu.getActiveSession(sessionId);
        session.close();

        return "close";
    }

}
