package Mofit.com.repository;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;



public interface RankRepository extends JpaRepository<Rank, String> {

}
