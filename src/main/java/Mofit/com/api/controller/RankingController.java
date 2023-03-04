package Mofit.com.api.controller;

import Mofit.com.Domain.Rank;
import Mofit.com.api.service.RankingService;
import Mofit.com.util.RankingComparatorScore;
import Mofit.com.util.RankingComparatorWin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

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
        return rankList(new RankingComparatorWin());
    }
    @GetMapping("/ranking/single")
    public JSONArray rankListSingle() throws JsonProcessingException, ParseException {
        return rankList(new RankingComparatorScore());
    }

    private JSONArray rankList(Comparator<Rank> comparator) throws JsonProcessingException, ParseException {
        List<Rank> ranks = rankService.rankingList();

        ranks.sort(comparator);
        return (JSONArray) parser.parse(mapper.writeValueAsString(ranks));
    }

}
