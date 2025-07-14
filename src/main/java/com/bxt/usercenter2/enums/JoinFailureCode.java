package com.bxt.usercenter2.enums;

public enum JoinFailureCode {
    TEAM_NOT_EXIST(40001, "队伍不存在"),
    TEAM_FULL(40002, "队伍已满"),
    TEAM_NOT_JOINABLE(40003, "队伍不可加入"),
    TEAM_PASSWORD_INCORRECT(40004, "密码错误"),
    USER_ALREADY_IN_TEAM(40005, "用户已在队伍中"),
    USER_NOT_AUTHORIZED(40006, "用户未授权"),
    REFUSE_JOIN_REQUEST(40007, "拒绝加入请求");

    private final int code;
    private final String message;

    JoinFailureCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
