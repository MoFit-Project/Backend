package Mofit.com.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindRoomReq {
    private String roomId;

    private String password;
}
