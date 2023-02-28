package Mofit.com.exception.custom;

import Mofit.com.exception.EntityNotFoundException;

public class RoomNotFoundException extends EntityNotFoundException {
    public RoomNotFoundException(String roomId) {
        super(roomId +"room is not found");
    }
}
