package Mofit.com.api.controller;

import Mofit.com.Domain.Rank;
import Mofit.com.api.request.GameEndReq;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;


@Slf4j
@RestController
@RequestMapping("/mofit")
public class GameController {

    private final RankingService rankService;

    public GameController(RankingService rankService) {
        this.rankService = rankService;
    }



    @PostMapping("/result/single")
    public ResponseEntity<String> gameResultSingle(@RequestBody GameEndReq request){
        return rankService.updateRankScore(request);
    }


}
