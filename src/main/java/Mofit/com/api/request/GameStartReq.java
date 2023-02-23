package Mofit.com.api.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameStartReq {
    private String session;
    private List<String> to;
    private String type;
    private String data;

}
