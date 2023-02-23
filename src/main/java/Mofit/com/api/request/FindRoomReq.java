package Mofit.com.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindRoomReq {
    private String roomId;

    private String password;
}
//@Getter
//@Setter
//@ApiModel("FindRoomRequest")
//public class FindRoomReq {
//
//    @ApiModelProperty(name = "방 번호", example="QB8TKZC05P")
//    @NotEmpty(message = "roomId may not be empty")
//    @Size(max = 50)
//    private String roomId;
//
//    @ApiModelProperty(name = "비밀번호", example="password")
//    @NotNull(message = "password may not be empty")
//    @Size(max = 50)
//    private String password;