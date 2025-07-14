package com.bxt.usercenter2.service.impl;

//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
////import generator.service.MailService;
//import com.bxt.usercenter2.mapper.MailMapper;
//import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.enums.JoinFailureCode;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.mapper.MailMapper;
import com.bxt.usercenter2.model.domain.Mail;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.MailService;
import com.bxt.usercenter2.service.TeamService;
import com.bxt.usercenter2.vo.MailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author bxt
* @description 针对表【mail】的数据库操作Service实现
* @createDate 2025-07-13 17:44:23
*/
@Service
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail>
    implements MailService {
    //查看已发送的邮件
    @Override
    public Page<MailVO> presentSentMails(user loginUser, long pageNum, long pageSize) {
        return queryMailsByField("sendUserId", loginUser.getId(), pageNum, pageSize);
    }
    // 查看已接收的邮件
    @Override
    public Page<MailVO> presentReceivedMails(user loginUser, long pageNum, long pageSize) {
        return queryMailsByField("receiveUserId", loginUser.getId(), pageNum, pageSize);
    }

    // 私有通用方法
    private Page<MailVO> queryMailsByField(String fieldName, Long userId, long pageNum, long pageSize) {
        Page<Mail> mailPage = new Page<>(pageNum, pageSize);
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, userId)
                .eq("isDelete", 0)
                .orderByAsc("haveRead")
                .orderByDesc("createTime");

        Page<Mail> mailPageResult = page(mailPage, queryWrapper);

        List<MailVO> mailVOList = mailPageResult.getRecords().stream()
                .map(mail -> {
                    MailVO mailVO = new MailVO();
                    BeanUtils.copyProperties(mail, mailVO);
                    return mailVO;
                })
                .toList();

        Page<MailVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setRecords(mailVOList);
        resultPage.setTotal(mailPageResult.getTotal());

        if (mailVOList.isEmpty()) {
            // 如果没有找到相关邮件，抛出异常
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关邮件");
        }

        return resultPage;
    }
    // 设置为已读邮件
    @Override
    public boolean markMailAsRead(user loginUser, Long mailId) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mailId", mailId)
                .eq("receiveUserId", loginUser.getId())
                .eq("haveRead", 0)
                .eq("isDelete", 0);

        List<Mail> mails = list(queryWrapper);
        if (mails.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到未读邮件或邮件已被删除");
        }

        for (Mail mail : mails) {
            mail.setHaveRead(1); // 设置为已读
        }

        return updateBatchById(mails);
    }
    // 全部已读功能
    @Override
    public boolean markAllAsRead(user loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId())
                .eq("haveRead", 0)
                .eq("isDelete", 0);

        List<Mail> unreadMails = list(queryWrapper);
        if (unreadMails.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有未读邮件");
        }

        for (Mail mail : unreadMails) {
            mail.setHaveRead(1); // 设置为已读
        }

        return updateBatchById(unreadMails);
    }

    //删除已读邮件
    @Override
    public boolean deleteReadMail(user loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiveUserId", loginUser.getId())
                .eq("haveRead", 1)
                .eq("isDelete", 0);

        List<Mail> mails = list(queryWrapper);
        for (Mail mail : mails) {
            mail.setIsDelete(1); // 设置为已删除
        }
        if (mails.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到已读邮件或邮件已被删除");
        }
        return updateBatchById(mails);
    }
    @Autowired
    @Lazy // 使用 @Lazy 注解以避免循环依赖
    private TeamService teamService;
    //全部同意
    @Override
    public long agreeAllJoinTeam(user loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        List<Mail> mails = list(new QueryWrapper<Mail>()
                .eq("receiveUserId", loginUser.getId())
                .eq("mailType", 1)
                .eq("haveRead", 0)
                .eq("isDelete", 0));
        long count=0;
        boolean success;
        for (Mail mail : mails) {
            success = agreeJoinTeam(loginUser, mail.getMailId());
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "同意加群操作失败");
            }
            else if (success){
                count+=1;
            }
        }
        return count;
    }
    //全部拒绝
    @Override
    public long refuseAllJoinTeam(user loginUser, String reason) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        List<Mail> mails = list(new QueryWrapper<Mail>()
                .eq("receiveUserId", loginUser.getId())
                .eq("mailType", 1)
                .eq("haveRead", 0)
                .eq("isDelete", 0));
        long count=0;
        boolean success;
        for (Mail mail : mails) {
            success = refuseJoinTeam(loginUser, mail.getMailId());
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "拒绝加群操作失败");
            }
            else if (success){
                count+=1;
            }
        }
        return count;
    }
    //同意加群
    @Override
    public boolean agreeJoinTeam(user loginUser, Long mailId) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        Mail mail=MailServiceImpl.this.getById(mailId);
        if (mail ==null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关邮件");
        }
        if (!Objects.equals(mail.getReceiveUserId(), loginUser.getId())) {
            System.out.println(loginUser.getId() + "尝试查看邮件：" + mailId);
            System.out.println("查看邮件：" + mailId + "，发送者ID：" + mail.getSendUserId() + "，接收者ID：" + mail.getReceiveUserId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您没有权限操作此邮件");
        }
        if (mail.getMailType() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此邮件不是申请入群邮件");
        }
        // 获取相关的团队信息
        boolean send;
        Team team = teamService.getById(mail.getRelatedTeam());
        if (team == null) {
            send=sendJoinFailureMail(loginUser,mail.getSendUserId(), mail.getRelatedTeam(), JoinFailureCode.TEAM_NOT_EXIST.getMessage());
            if (!send) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送加群失败邮件失败");
            }
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关团队");
        }
        if (team.getExpireTime() != null && team.getExpireTime().before(new java.util.Date())) {
            send=sendJoinFailureMail(loginUser,mail.getSendUserId(), mail.getRelatedTeam(), JoinFailureCode.TEAM_NOT_EXIST.getMessage());
            if (!send) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送加群失败邮件失败");
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "团队已过期，无法加入");
        }
        if (team.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此团队不是公开但经审核可加入的状态");
        }
        if (team.getMaxNum() <= teamService.getSumPeople(team)) {
            send=sendJoinFailureMail(loginUser,mail.getSendUserId(), mail.getRelatedTeam(), JoinFailureCode.TEAM_FULL.getMessage());
            if (!send) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送加群失败邮件失败");
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "团队已满，无法加入");
        }
        // 将用户加入团队
        boolean isAdded = teamService.addUserToTeam(mail.getSendUserId(), team.getId());
        if (!isAdded) {
            send=sendJoinFailureMail(loginUser,mail.getSendUserId(), mail.getRelatedTeam(), JoinFailureCode.TEAM_NOT_JOINABLE.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法将用户加入团队");
        }
        send=sendJoinSuccessMail(loginUser, mail.getSendUserId(), mail.getRelatedTeam());
        return isAdded;
    }
    //拒绝加群
    @Override
    public boolean refuseJoinTeam(user loginUser, Long mailId) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        Mail mail = MailServiceImpl.this.getById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关邮件");
        }
        if (mail.getReceiveUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您没有权限操作此邮件");
        }
        if (mail.getMailType() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "此邮件不是申请入群邮件");
        }
        // 发送拒绝加入的邮件
        boolean send = sendJoinFailureMail(loginUser, mail.getSendUserId(), mail.getRelatedTeam(), JoinFailureCode.REFUSE_JOIN_REQUEST.getMessage());
        if (!send) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送拒绝加入邮件失败");
        }
        return send;
    }
    // 发送加群成功的消息
    private boolean sendJoinSuccessMail(user loginUser, Long receivedUser, Long teamId) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关团队");
        }
        Mail mail = new Mail();
        mail.setSendUserId(loginUser.getId());
        mail.setReceiveUserId(receivedUser);
        mail.setRelatedTeam(teamId);
        mail.setMessage("您已成功加入团队 " + team.getName());
        mail.setHaveRead(0);
        mail.setIsDelete(0);
        mail.setCreateTime(new java.util.Date());
        return save(mail);
    }
    // 发送加群失败的消息
    private boolean sendJoinFailureMail(user loginUser, Long receivedUser, Long teamId, String reason) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到相关团队");
        }
        Mail mail = new Mail();
        mail.setSendUserId(loginUser.getId());
        mail.setReceiveUserId(receivedUser);
        mail.setRelatedTeam(teamId);
        mail.setMessage("您申请加入团队 " + team.getName() + " 已被拒绝，原因是："+reason);
        mail.setHaveRead(0);
        mail.setIsDelete(0);
        mail.setCreateTime(new java.util.Date());
        return save(mail);
    }
    //查看某个邮件详情
    @Override
    public MailVO viewMail(Long mailId, user loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录或ID无效");
        }
        System.out.println("查看邮件，邮件ID：" + mailId + "，用户ID：" + loginUser.getId());
        Mail mail = getById(mailId);
        if (mail == null || mail.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "邮件不存在或已被删除");
        }
        if (!mail.getReceiveUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您没有权限查看此邮件");
        }
        System.out.println("查看邮件：" + mailId + "，发送者ID：" + mail.getSendUserId() + "，接收者ID：" + mail.getReceiveUserId());
        // 设置为已读
        mail.setHaveRead(1);
        updateById(mail);

        MailVO mailVO = new MailVO();
        BeanUtils.copyProperties(mail, mailVO);
        return mailVO;
    }

}




