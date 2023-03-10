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


    @CachePut(value ="user_rank", key = "#userId",cacheManager = "myCacheManager")
    public void updateRankWin(String winId, String userId) {
        List<Rank> ranks = rankingList();
        for (Rank rank : ranks) {
            if(rank.getId().equals(userId)){
                if(Objects.equals(winId, userId)){
                    rank.setWin(rank.getWin() + 1);
                }
                rank.setGames(rank.getGames()+1);

                rankRepository.save(rank);
            }
        }

    }

    @CachePut(value ="user_score", key = "#request.userId",cacheManager = "myScoreManager")
    public ResponseEntity<String> updateRankScore(GameEndReq request) {
        List<Rank> ranks = rankingListScore();
        for (Rank rank : ranks) {
            if (rank.getId().equals(request.getUserId())) {
                double value = Double.parseDouble(request.getScore());

                if (rank.getScore() == 0) {
                    rank.setScore(value);

                } else if (value >= rank.getScore()) {
                    return new ResponseEntity<>("안함", HttpStatus.OK);
                }

                rank.setScore(value);
                rankRepository.save(rank);

                return new ResponseEntity<>("OK",HttpStatus.OK);

            }
        }
            return new ResponseEntity<>("ID 존재 안함", HttpStatus.BAD_REQUEST);
//        if (user == null) {
//           user = Rank.builder()
//                   .score(0d)
//                   .id(request.getUserId())
//                   .games(0)
//                   .win(0)
//                   .build();
//        }

//        double value = Double.parseDouble(request.getScore());
//
//        if (user.getScore() == 0) {
//            user.setScore(value);
//
//        } else if (value >= user.getScore()) {
//            return new ResponseEntity<>("안함", HttpStatus.OK);
//        }
//
//        user.setScore(value);
//        rankRepository.save(user);
//
//        return new ResponseEntity<>("OK",HttpStatus.OK);
    }


    @Cacheable(value ="user_rank", cacheManager = "myCacheManager")
    public List<Rank> rankingList() {
        return rankRepository.findAll();
    }

    @Cacheable(value ="user_score", cacheManager = "myScoreManager")
    public List<Rank> rankingListScore() {
        return rankRepository.findNonZeroScoreRecords();
    }

}
