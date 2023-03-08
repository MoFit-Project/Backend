package Mofit.com.api.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoomRes {

    private String RoomId;
    private String mode;
    private Integer participant;
    private String createTime;
}
