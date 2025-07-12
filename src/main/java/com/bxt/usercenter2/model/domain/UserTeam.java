package com.bxt.usercenter2.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户队伍关系
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
public class UserTeam {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField("userId")
    private Long userId;

    /**
     * 
     */
    @TableField("teamId")
    private Long teamId;

    /**
     * 
     */
    @TableField("joinTime")
    private Date joinTime;

    /**
     * 
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 0-no 1-yes
     */
    @TableField("isDelete")
    private Integer isDelete;
}