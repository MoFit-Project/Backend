package Mofit.com.exception.custom;

import Mofit.com.exception.EntityNotFoundException;

public class RoomNotAllowEnterException extends EntityNotFoundException {

    public RoomNotAllowEnterException(String roomId) {
        super(roomId +"room is full");
    }
}
