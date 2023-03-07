package Mofit.com.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakeRoomReq {
    private String roomId;
    private String roomName;
    private String userId;
    private String mode;
}
