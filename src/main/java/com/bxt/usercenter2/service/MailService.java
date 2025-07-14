package com.bxt.usercenter2.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxt.usercenter2.model.domain.Mail;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.vo.MailVO;

/**
* @author bxt
* @description 针对表【mail】的数据库操作Service
* @createDate 2025-07-13 17:44:23
*/
public interface MailService extends IService<Mail> {

    // 显示已发送的消息
    Page<MailVO> presentSentMails(user loginUser, long pageNum, long pageSize);

    Page<MailVO> presentReceivedMails(user loginUser, long pageNum, long pageSize);

    // 设置为已读邮件
    boolean markMailAsRead(user loginUser, Long mailId);

    // 全部已读功能
    boolean markAllAsRead(user loginUser);

    //删除已读邮件
    boolean deleteReadMail(user loginUser);

    //全部同意
    long agreeAllJoinTeam(user loginUser);

    //全部拒绝
    long refuseAllJoinTeam(user loginUser, String reason);

    //同意加群
    boolean agreeJoinTeam(user loginUser, Long mailId);

    //拒绝加群
    boolean refuseJoinTeam(user loginUser, Long mailId);

    //查看某个邮件详情
    MailVO viewMail(Long mailId, user loginUser);
}
