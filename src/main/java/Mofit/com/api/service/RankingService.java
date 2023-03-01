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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankingService{

    private final MemberRepository memberRepository;

    private final RankRepository rankRepository;


    @Cacheable(value ="user_rank", key = "#userId",cacheManager = "myCacheManager")
    public Rank getRank(String userId) {

        Rank userRank = rankRepository.findById(userId).orElse(null);
        if (userRank == null){
            Rank rank = Rank.builder()
                    .userId(userId)
                    .win(0)
                    .games(0)
                    .build();

            rankRepository.save(rank);
        }

        return rankRepository.findById(userId).orElse(null);
    }

    @CachePut(value ="user_rank", key = "#req.usedId",cacheManager = "myCacheManager")
    public Boolean updateRank(RankReq req) {
        Rank userRank = getRank(req.getUsedId());
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

    @Cacheable(value ="user_rank",key="rankRepository.findAll()", cacheManager = "myCacheManager")
    public List<Rank> rankingList() {
        return rankRepository.findAll();
    }
}
