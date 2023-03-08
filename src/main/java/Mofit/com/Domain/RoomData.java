package Mofit.com.Domain;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomData {
    @Id
    private String userId;
    private String sessionId;
    private Integer participant;
    private String mode;
    private String status;
    private Integer time;
    private String createTime;
    @ElementCollection
    private List<String> gamers = new ArrayList<>();


}
