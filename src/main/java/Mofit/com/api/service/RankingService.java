//package Mofit.com.api.service;
//
//import Mofit.com.Domain.Member;
//import Mofit.com.Domain.Ranking;
//import Mofit.com.api.request.RankReq;
//import Mofit.com.api.response.RankRes;
//import Mofit.com.repository.MemberRepository;
//import Mofit.com.repository.RankingRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class RankingService {
//
//    private final MemberRepository memberRepository;
//
//    private final RankingRepository rankRepository;
//
//
//    public Boolean insertRank(RankReq req) {
//
//        Optional<Member> check = memberRepository.findByAccount(req.getMember().getAccount());
//        if(check.isPresent()){
//            Optional<Ranking> ranks = rankRepository.findByMember(check.get());
//
//            return checkInRank(req, ranks);
//            }
//        return false;
//        }
//
//    private Boolean checkInRank(RankReq req, Optional<Ranking> ranks) {
//        if(ranks.isPresent()){
//            if (req.isWin()){
//                ranks.get().setWin(ranks.get().getWin()+1);
//            }
//            ranks.get().setGames(ranks.get().getGames()+1);
//            rankRepository.save(ranks.get());
//                    return true;
//        }
//        return false;
//    }
//
//    public List<Ranking> rankingList() {
//        return rankRepository.findAll();
//    }
//
//    public RankRes getRanking(String account) {
//        Optional<Member> check = memberRepository.findByAccount(account);
//
//        if (check.isPresent()) {
//            RankRes res = new RankRes();
//            Optional<Ranking> ranks = rankRepository.findByMember(check.get());
//            if (ranks.isEmpty()) {
//                // create
//                Ranking rank = Ranking.builder()
//                        .win(0)
//                        .games(0)
//                        .member(check.get())
//                        .build();
//                res.setGames(0);
//                res.setWin(0);
//
//                rankRepository.save(rank);
//            }
//            else{
//                res.setWin(ranks.get().getWin());
//                res.setGames(ranks.get().getGames());
//            }
//            return res;
//        }
//        return null;
//    }
//}
