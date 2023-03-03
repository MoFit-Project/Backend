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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false)
    private Integer win;
    @Column(nullable = false)
    private Integer games;
    @Column(nullable = false)
    private Integer score;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;
}
