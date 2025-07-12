package com.bxt.usercenter2.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TeamAddRequest {

    /**
     * 队伍名
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     *
     */
    private Integer maxNum;


    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer status;

    private String password;
    private Date expireTime;
}
