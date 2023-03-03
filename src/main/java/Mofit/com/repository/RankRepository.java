package Mofit.com.repository;

import Mofit.com.Domain.Member;
import Mofit.com.Domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findByName(String userName);
}
