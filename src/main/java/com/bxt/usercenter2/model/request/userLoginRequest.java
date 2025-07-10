package com.bxt.usercenter2.model.request;

import lombok.Data;

@Data
public class userLoginRequest {
    private String userAccount;
    private String userPwd;
}
