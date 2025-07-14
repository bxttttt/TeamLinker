package com.bxt.usercenter2.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class MailVO {

    private Long sendUserId;

    private Long receiveUserId;

    private Long relatedTeam;

    private String message;

    private Integer haveRead;


    private Date createTime;



    private Long mailId;

    private Integer mailType; // 0-普通邮件 1-申请入群


}
