package Mofit.com.util;

import Mofit.com.Domain.Rank;
import Mofit.com.api.response.RoomRes;

import java.util.Comparator;

public class RoomsComparator implements Comparator<RoomRes> {
    @Override
    public int compare(RoomRes o1, RoomRes o2) {

        return o2.getCreateTime().compareTo(o1.getCreateTime());
    }

}