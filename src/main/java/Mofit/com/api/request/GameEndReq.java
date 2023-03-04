package Mofit.com.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameEndReq {
    private String userId;

    private Integer isWin;
}
