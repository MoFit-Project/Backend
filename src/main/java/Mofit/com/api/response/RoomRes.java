package Mofit.com.api.response;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RoomRes {
    private String userId;
    private String roomId;
    private Integer participant;
    private String mode;

    private String status;
}
