package Mofit.com.exception;

public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Invalid Input Value"),
    ENTITY_NOT_FOUND(404, "C003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),
    UNAUTHORIZED(401, "C007", "UnAuthorized"),


    // User
    EMAIL_DUPLICATION(409, "U001", "Email is Duplication"),
    AUTH_EMAIL_SEND_FAIL(500, "U002", "Auth Email Send Fail"),
    NICKNAME_DUPLICATION(409, "U003", "Nickname is Duplication"),
    CURRENT_PASSWORD_NOT_MATCH_EXCEPTION(400, "U004", "Current password not match exception"),
    LOGIN_DUPLICATION(409, "U005", "Current user is logined. You can not login to this account"),


    // Room
    ROOM_IS_NOT_AVAILABLE(409, "R001", "Room Status is Not Available"),
    ROOM_IS_FULL(409, "R001", "Room is Full"),
    ROOM_IS_ON_GAME(409, "R002", "Room is on Game"),

    ;


    private final String code;
    private final String message;
    private int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}