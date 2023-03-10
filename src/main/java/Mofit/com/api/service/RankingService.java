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


    @CacheEvict(value ="user_rank", key = "#userId",cacheManager = "myCacheManager")
    public void updateRankWin(String winId, String userId) {
        Rank id = getRankById(userId);
        if (id == null){
            throw new EntityNotFoundException("존재하지 않는 Id 입니다");
        }
        if(Objects.equals(winId, id.getId())){
            id.setWin(id.getWin() + 1);
        }
        id.setGames(id.getGames()+1);

        rankRepository.save(id);

    }

    @CacheEvict(value ="user_rank", key = "#request.userId",cacheManager = "myScoreManager")
    public ResponseEntity<String> updateRankScore(GameEndReq request) {
        Rank user = getRankById(request.getUserId());

        double value = Double.parseDouble(request.getScore());

        if (user.getScore() == 0) {
            user.setScore(value);

        } else if (value >= user.getScore()) {
            return new ResponseEntity<>("안함", HttpStatus.OK);
        }

        user.setScore(value);
        rankRepository.save(user);

        return new ResponseEntity<>("OK",HttpStatus.OK);
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
