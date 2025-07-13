package com.bxt.usercenter2.model.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName mail
 */
@TableName(value ="mail")
@Data
public class Mail {
    /**
     * 
     */
    @TableField("sendUserId")
    private String sendUserId;

    /**
     * 
     */
    @TableField("receiveUserId")
    private String receiveUserId;

    /**
     * 
     */
    @TableField("relatedTeam")
    private String relatedTeam;

    /**
     * 
     */
    @TableField("message")
    private String message;

    /**
     * 
     */
    @TableField("isDelete")
    private Integer isDelete;

    /**
     * 0-未读 1-已读
     */
    @TableField("haveRead")
    private Integer haveRead;

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
}