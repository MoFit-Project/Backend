package Mofit.com.api.controller;

import Mofit.com.Domain.Rank;
import Mofit.com.Domain.Room;
import Mofit.com.Domain.RoomData;
import Mofit.com.api.request.GameEndReq;
import Mofit.com.api.request.GameLeaveReq;
import Mofit.com.api.response.ResultRes;
import Mofit.com.api.service.RankingService;
import Mofit.com.api.service.RoomService;
import Mofit.com.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RestController
@RequestMapping("/mofit")
public class GameController {

    private final RankingService rankService;
    private static final long DELAY = 5L;
    public GameController(RankingService rankService) {
        this.rankService = rankService;
    }


    @PostMapping("/game/{roomId}")
    public Mono<ResultRes> resultSignal(@PathVariable String roomId, @RequestBody ResultRes request) {

        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            throw new EntityNotFoundException("존재하지 않는 방입니다!");
        }
        RoomData roomRes = roomData.getRes();

        ResultRes dto = new ResultRes();

        dto.setSession(roomRes.getSessionId());
        dto.setTo(request.getTo());
        dto.setType("result");
        dto.setData("Game End");

        return RoomService.postMessage(dto, ResultRes.class);
    }


    @GetMapping("/game/{roomId}")
    @Async
    public CompletableFuture<GameLeaveReq> startSignal(@PathVariable String roomId) {
        log.info("POST GAME START");

        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            throw new EntityNotFoundException("존재하지 않는 방입니다!");
        }
        RoomData roomRes = roomData.getRes();

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(roomRes.getSessionId());
        dto.setType("start");
        dto.setData("Let's Start");

        long delaySeconds = DELAY + roomRes.getTime();
        return RoomService.postMessage(dto, GameLeaveReq.class)
                .then(Mono.delay(Duration.ofSeconds(delaySeconds)))
                .then(RoomService.endSignal(roomId).subscribeOn(Schedulers.boundedElastic()))
                .toFuture()
                .exceptionally(ex -> {
                    log.error("Error occurred: " + ex.getMessage());
                    return null;
                });
    }

    @PostMapping("/result/single")
    public ResponseEntity<String> gameResultSingle(@RequestBody GameEndReq request){

        return rankService.updateRankScore(request);
    }
    @PostMapping("/result/{roomId}")
    public ResponseEntity<String> gameResultMulti(@PathVariable String roomId,@RequestBody GameEndReq request){

        Room roomData = RoomService.findRoom(roomId);
        if (roomData == null) {
            return new ResponseEntity<>("존재하지 방입니다", HttpStatus.BAD_REQUEST);
        }

        RoomData room = roomData.getRes();
        if (room == null) {
            return new ResponseEntity<>("존재하지 않는 유저", HttpStatus.BAD_REQUEST);
        }
        room.getGamers().forEach(gamer -> rankService.updateRankWin(request.getUserId(),gamer));

        return new ResponseEntity<>("OK",HttpStatus.OK);
    }

}
