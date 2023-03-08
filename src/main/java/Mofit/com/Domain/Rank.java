package Mofit.com.Domain;

import lombok.*;


import javax.persistence.*;

@Entity
@Table(name = "user_rank")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rank {
    @Id
    private String id;
    @Column(nullable = false)
    private Integer win;
    @Column(nullable = false)
    private Integer games;
    @Column(nullable = false)
    private Double score;

}
