package Mofit.com.exception.custom;

import Mofit.com.exception.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(String userId) {
        super(userId +"User not Found");
    }
}
