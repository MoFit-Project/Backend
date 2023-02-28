//package Mofit.com.api.controller;
//
//import Mofit.com.Domain.Member;
//import Mofit.com.api.request.SignReq;
//import Mofit.com.api.response.RankRes;
//import Mofit.com.api.response.SignRes;
//import Mofit.com.api.service.RankingService;
//import Mofit.com.api.service.SignService;
//import Mofit.com.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/mofit")
//public class RankingController {
//
//    private final RankingService rankingService;
//
//    private final SignService memberService;
//
//
//
//    @GetMapping("/ranking/{account}")
//    public ResponseEntity<RankRes> getRank(@PathVariable String account)  {
//        RankRes ranking = rankingService.getRanking(account);
//
//        if (ranking == null) {
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
////////////////////////////////////////////////////고쳐야됌
//        return null;
//
//    }
//
//
//}
