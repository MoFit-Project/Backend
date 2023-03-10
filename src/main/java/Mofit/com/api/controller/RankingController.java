package Mofit.com.api.controller;

import Mofit.com.Domain.Rank;
import Mofit.com.api.service.RankingService;
import Mofit.com.util.RankingComparatorScore;
import Mofit.com.util.RankingComparatorWin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mofit")
public class RankingController {
    private final RankingService rankService;

    JSONParser parser = new JSONParser();
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    public RankingController(RankingService rankService) {
        this.rankService = rankService;
    }

    @GetMapping("/ranking/multi")
    public JSONArray rankListMulti() throws JsonProcessingException, ParseException {
        return rankList(rankService.rankingList(),new RankingComparatorWin());
    }
    @GetMapping("/ranking/single")
    public JSONArray rankListSingle() throws JsonProcessingException, ParseException {
        return rankList(rankService.rankingList().stream()
                .filter(rank -> rank.getScore() > 0) // score 값이 0인 것 필터링
                .sorted(new RankingComparatorScore())
                .collect(Collectors.toList()), null);
    }

    private JSONArray rankList(List<Rank> ranks,Comparator<Rank> comparator) throws JsonProcessingException, ParseException {

        long start = System.currentTimeMillis();

        log.info("start Time = {}ms", start);
        ranks.sort(comparator);
        long finish = System.currentTimeMillis();
        long timeMs = finish - start;
        log.info("END : {}ms ",timeMs);

        return (JSONArray) parser.parse(mapper.writeValueAsString(ranks));
    }

}
