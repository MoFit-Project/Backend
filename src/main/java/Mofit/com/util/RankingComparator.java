package Mofit.com.util;
import Mofit.com.Domain.Rank;


import java.util.Comparator;

public class RankingComparator implements Comparator<Rank> {
    @Override
    public int compare(Rank o1, Rank o2) {
        return o2.getWin().compareTo(o1.getWin());
    }

}
