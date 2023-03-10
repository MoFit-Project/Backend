package Mofit.com.api.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnterRoomRes {

    private String sessionId;

    private Integer time;
    private String mode;
}
