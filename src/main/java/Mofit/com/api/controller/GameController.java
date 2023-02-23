package Mofit.com.api.controller;

import Mofit.com.api.request.GameStartReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/gameStart")
    public Mono<GameStartReq> startSignal(@RequestBody GameStartReq request) {
        log.info("POST GAME START");

        GameStartReq dto = new GameStartReq();
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
                .bodyToMono(GameStartReq.class);
    }





}
