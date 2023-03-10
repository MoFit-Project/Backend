package Mofit.com.util;

import Mofit.com.Domain.Rank;

import java.util.Comparator;

public class RankingComparatorScore implements Comparator<Rank> {
    @Override
    public int compare(Rank o1, Rank o2) {
        return o1.getScore().compareTo(o2.getScore());
    }

}
