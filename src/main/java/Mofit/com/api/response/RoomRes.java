package Mofit.com.api.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class RoomRes {
    private String userId;
    private String roomId;
    private Integer participant;
    private String mode;
    private String status;
    private Integer time;
    private LocalDateTime createTime;


}
