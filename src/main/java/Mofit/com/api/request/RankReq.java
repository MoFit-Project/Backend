package Mofit.com.api.request;

import Mofit.com.Domain.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankReq {

    private String  userId;
    private boolean win;
    private Integer games;

    private Long score;

}
