package com.bxt.usercenter2.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamUserVO {
    private Long teamId;
    private String teamName;
    private String teamDescription;
    private Integer teamMaxNum;
    private Date teamExpireTime;
    private int status; // 0-公开且任何人可加入 1-公开但经审核可加入 2-加密 3-私有
    private int sumPeople; // 队伍成员数量
    private Long userId; // 创建者的用户ID
    private String userAvatar; // 创建者的头像
    private List<Long> userIdList; // 队伍成员ID列表
    private List<String> userAvatarList; // 队伍成员头像列表






}
