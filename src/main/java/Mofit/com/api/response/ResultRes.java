package Mofit.com.api.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultRes {
    private String session;
    private List<String> to;
    private String type;
    private String data;

}
