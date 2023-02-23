package Mofit.com.Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    private String roomId;
    private Integer gameType;
    private boolean isPublicKey;
    private String password;
    private String status;
    private Timestamp createdAt;


}
