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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @CachePut(value ="user_rank", key = "#req.usedId",cacheManager = "myCacheManager")
    public Boolean updateRankWin(RankReq req) {
        Rank userRank = getRankById(req.getUsedId());
        if(userRank == null){
            throw new EntityNotFoundException(req.getUsedId());
        }

        if(req.isWin()){
            userRank.setWin(userRank.getWin()+1);
        }
        userRank.setGames(userRank.getGames() + 1);
        rankRepository.save(userRank);
        return true;
    }

    @CachePut(value ="user_rank", key = "#req.usedId",cacheManager = "myCacheManager")
    public Boolean updateRankScore(RankReq req) {
        Rank userRank = getRank(req.getUsedId());
        if(userRank == null){
            throw new EntityNotFoundException(req.getUsedId());
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
}
