package com.bxt.usercenter2.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
    @TableField("maxNum")
    private Integer maxNum;

    /**
     * 
     */
    @TableField("expireTime")
    private Date expireTime;

    /**
     * 用户id
     */
    @TableField("userId")
    private Long userId;

    /**
     * 0-公开且任何人可加入 1-公开但经审核可加入 2-加密 3-私有
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 0-未删除 1-删除
     */
    @TableField("isDelete")
    private Integer isDelete;
}