package Mofit.com.api.controller;

import Mofit.com.api.request.GameLeaveReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;


@Slf4j
@RestController
@RequestMapping("/mofit")
public class GameController {

    private final WebClient webClient;

    String credentials = "OPENVIDUAPP:MY_SECRET";
    String encodedCredentials = new String(Base64.getEncoder().encode(credentials.getBytes()));
    public GameController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://ena.jegal.shop:8443").build();
    }

    @PostMapping("/result/{gameMode}")
    public String gameResult(@PathVariable String gameMode){
        return "ok";
    } // JSon 모델 req


    @PostMapping("/gameStart")
    public Mono<GameLeaveReq> startSignal(@RequestBody GameLeaveReq request) {
        log.info("POST GAME START");

        GameLeaveReq dto = new GameLeaveReq();
        dto.setSession(request.getSession());
        dto.setTo(request.getTo());
        dto.setType("start");
        dto.setData("Let's Start");

        return webClient.post()
                .uri("/openvidu/api/signal")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(GameLeaveReq.class);
    }

}
