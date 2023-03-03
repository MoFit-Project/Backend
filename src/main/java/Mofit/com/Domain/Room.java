package Mofit.com.Domain;


import lombok.*;


import javax.persistence.Entity;
import javax.persistence.Id;



@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    private String roomId;
    private String roomName;
    private String mode;



}
