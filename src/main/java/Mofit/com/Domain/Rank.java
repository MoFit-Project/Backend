package Mofit.com.Domain;

import lombok.*;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_rank")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rank {
    @Id
    private String userId;
    private Integer win;
    private Integer games;

}
