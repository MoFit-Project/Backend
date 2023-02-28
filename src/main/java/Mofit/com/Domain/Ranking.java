//package Mofit.com.Domain;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.*;
//
//import javax.persistence.*;
//
//@Entity
//@Getter
//@Setter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class Ranking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @JoinColumn(name = "member")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    private Member member;
//
//    private Integer win;
//
//    private Integer games;
//
//}
