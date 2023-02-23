package Mofit.com.api.service;

import Mofit.com.Domain.RoomDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.openvidu.java.client.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OpenviduService {

    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();

    public List<RoomDTO> getRoomMap(List<Session> activeSessions) throws  ParseException, JsonProcessingException {

        JSONArray jArrary = (JSONArray) parser.parse(mapper.writeValueAsString(activeSessions));

        List<RoomDTO> roomData = new ArrayList<>();

        jArrary.forEach(arr -> {
            RoomDTO dto = new RoomDTO();

            JSONObject obj = (JSONObject) arr;
            JSONArray arrary = (JSONArray) obj.get("connections");

            dto.setRoomId((String) obj.get("sessionId"));
            dto.setParticipant(arrary.size());
            roomData.add(dto);
        });
        return roomData;
    }


}
