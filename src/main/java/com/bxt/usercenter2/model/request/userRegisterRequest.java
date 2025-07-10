package com.bxt.usercenter2.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class userRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 319124L;

    private String userAccount;
    private String userPwd;
    private String checkPwd;

}
