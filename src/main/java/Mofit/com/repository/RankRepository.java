package Mofit.com.repository;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Repository
public interface RankRepository extends JpaRepository<Rank, String> {
    @Query("SELECT e FROM Rank e where e.score<> 0")
    List<Rank> findAllWithScoreNotZero();
}
