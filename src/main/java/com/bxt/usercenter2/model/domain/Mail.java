package com.bxt.usercenter2.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId(type = IdType.AUTO,value = "mailId")
    private Long mailId;

    /**
     * 
     */
    @TableField("sendUserId")
    private Long sendUserId;

    /**
     * 
     */
    @TableField("receiveUserId")
    private Long receiveUserId;

    /**
     * 
     */
    @TableField("relatedTeam")
    private Long relatedTeam;

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

    /**
     * 0-普通信件 1-申请入群的信件
     */
    @TableField("mailType")
    private Integer mailType;
}