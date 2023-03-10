package Mofit.com.api.service;

import Mofit.com.Domain.Rank;
import Mofit.com.api.request.GameEndReq;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankingService{

    private final RankRepository rankRepository;


    public Rank getRankById(String userId) {
        return rankRepository.findById(userId).orElse(null);
    }


    @CachePut(value ="user_rank",key = "'user_rank_list'",cacheManager = "myCacheManager")
    public List<Rank> updateRankWin(String winId, List<String> gamers) {
        List<Rank> ranks = rankingList();
        gamers.forEach(gamer-> ranks.stream()
                .filter(rank -> rank.getId().equals(gamer))
                .forEach(rank -> {
                    if (winId.equals(gamer)) {
                        rank.setWin(rank.getWin() + 1);
                    }
                    rank.setGames(rank.getGames() + 1);
                    rankRepository.save(rank);
                }));

        return ranks;
    }

    @CachePut(value = "user_score", key = "'user_score_list'", cacheManager = "myCacheManager")
    public List<Rank> updateRankScore(GameEndReq request) {
        List<Rank> ranks = rankingListScore();

        ranks.stream()
                .filter(rank -> rank.getId().equals(request.getUserId()))
                .findFirst()
                .ifPresent(rank -> {
                    double value = Double.parseDouble(request.getScore());
                    if (rank.getScore() == 0) {
                        rank.setScore(value);
                    } else if (value >= rank.getScore()) {
                        return;
                    }
                    rank.setScore(value);
                    rankRepository.save(rank);
                });

        return ranks;
    }




    @Cacheable(value ="user_rank", key = "'user_rank_list'", cacheManager = "myCacheManager")
    public List<Rank> rankingList() {
        return rankRepository.findAll();
    }

    @Cacheable(value = "user_score", key = "'user_score_list'", cacheManager = "myCacheManager")
    public List<Rank> rankingListScore() {
        return rankRepository.findNonZeroScoreRecords();
    }


}
