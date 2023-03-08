package Mofit.com.Domain;


import lombok.*;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    private String roomId;
    @OneToOne(cascade = CascadeType.ALL)
    private RoomData res;



}
