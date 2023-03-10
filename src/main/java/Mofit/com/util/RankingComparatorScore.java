package Mofit.com.util;

import Mofit.com.Domain.Rank;

import java.util.Comparator;

public class RankingComparatorScore implements Comparator<Rank> {
    @Override
    public int compare(Rank o1, Rank o2) {
        double score1 = o1.getScore();
        double score2 = o2.getScore();

        // score 값이 0인 경우에는 비교하지 않음
        if (score1 == 0 || score2 == 0) {
            return 0;
        }

        return Double.compare(score2, score1);
    }

}
