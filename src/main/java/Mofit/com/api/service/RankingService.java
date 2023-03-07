package Mofit.com.api.service;

import Mofit.com.Domain.Rank;
import Mofit.com.api.request.RankReq;
import Mofit.com.exception.EntityNotFoundException;
import Mofit.com.repository.MemberRepository;
import Mofit.com.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankingService{

    private final MemberRepository memberRepository;

    private final RankRepository rankRepository;


    public Rank getRankById(String userId) {
        return rankRepository.findById(userId).orElse(null);
    }




    @CachePut(value ="user_rank", key = "#userId",cacheManager = "myCacheManager")
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

    @CachePut(value ="user_rank", key = "#req.userId",cacheManager = "myCacheManager")
    public Boolean updateRankScore(RankReq req) {
        Rank userRank = getRankById(req.getUserId());
        if(userRank == null){
            throw new EntityNotFoundException(req.getUserId());
        }

        if(req.getScore() > userRank.getScore()){
            userRank.setScore(req.getScore());
        }
        else{
            return true;
        }

        rankRepository.save(userRank);
        return true;
    }


    @Cacheable(value ="user_rank", cacheManager = "myCacheManager")
    public List<Rank> rankingList() {
        return rankRepository.findAll();
    }

    @Cacheable(value ="user_rank", cacheManager = "myCacheManager")
    public List<Rank> rankingListScore() {
        return rankRepository.findAllWithScoreNotZero();
    }

}
