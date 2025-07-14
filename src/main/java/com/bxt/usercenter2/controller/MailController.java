package com.bxt.usercenter2.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
import com.bxt.usercenter2.dto.PageRequest;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.MailService;
import com.bxt.usercenter2.service.userService;
import com.bxt.usercenter2.vo.MailVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {
    // 这里可以添加邮件相关的处理方法，例如发送邮件、查看邮件等
    // 目前这个类只是一个占位符，具体实现可以根据需求添加

    // 示例方法：发送邮件
    // @PostMapping("/send")
    // public ResponseEntity<String> sendMail(@RequestBody MailRequest mailRequest) {
    //     // 实现发送邮件的逻辑
    //     return ResponseEntity.ok("邮件已发送");
    // }
    // 查看收件箱
    @Resource
    private MailService mailService;
    @Resource
    private userService userService;
    // 查看收件箱
    @GetMapping("/checkInbox")
    public BaseResponse<Page<MailVO>> checkInbox(@RequestBody PageRequest pageRequest, HttpServletRequest httpServletRequest) {
        // 这里调用MailService的相关方法来获取收件箱的邮件
        // MailService mailService = ...; // 获取MailService的实例
        // Page<MailVO> mailPage = mailService.presentReceivedMails(loginUser, pageNum, pageSize);
        // return ResultUtils.success(mailPage);
        long pageNum = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页码或页面大小无效");
        }
        // 获取登录用户
        user loginUser =userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Page<MailVO> mailPage = mailService.presentReceivedMails(loginUser, pageNum, pageSize);
        return ResultUtils.success(mailPage); // 需要实现具体逻辑
    }
    // 查看发件箱
    @GetMapping("/checkSent")
    public BaseResponse<Page<MailVO>> checkSent(@RequestBody PageRequest pageRequest, HttpServletRequest httpServletRequest) {
        long pageNum = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页码或页面大小无效");
        }
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Page<MailVO> mailPage = mailService.presentSentMails(loginUser, pageNum, pageSize);
        return ResultUtils.success(mailPage); // 需要实现具体逻辑
    }
    // 全部已读
    @PostMapping("/markAllAsRead")
    public BaseResponse<Boolean> markAllAsRead(HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = mailService.markAllAsRead(loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法标记所有邮件为已读");
        }
        return ResultUtils.success(result);
    }
    // 全部同意
    @PostMapping("/agreeAll")
    public BaseResponse<Long> agreeAll(HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 这里需要实现同意所有邮件请求的逻辑
        long count = mailService.agreeAllJoinTeam(loginUser);
        return ResultUtils.success(count);
    }
    // 全部拒绝
    @PostMapping("/refuseAll")
    public BaseResponse<Long> refuseAll(@RequestParam String reason, HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 这里需要实现拒绝所有邮件请求的逻辑
        long count = mailService.refuseAllJoinTeam(loginUser, reason);

        return ResultUtils.success(count);
    }
    // 删除已读邮件
    @PostMapping("/deleteReadMail")
    public BaseResponse<Boolean> deleteReadMail(HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = mailService.deleteReadMail(loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法删除邮件");
        }
        return ResultUtils.success(result);
    }
    // 查看某个邮件详情
    @GetMapping("/viewMail")
    public BaseResponse<MailVO> viewMail(@RequestParam Long mailId, HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 调用MailService查看邮件详情
        MailVO mailVO = mailService.viewMail(mailId,loginUser);
        if (mailVO == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "邮件不存在或已被删除");
        }
        return ResultUtils.success(mailVO);
    }
    // 同意某个邮件请求
    @PostMapping("/agreeJoinTeam")
    public BaseResponse<Boolean> agreeJoinTeam(@RequestParam Long mailId, HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 调用MailService同意某个邮件请求
        boolean result = mailService.agreeJoinTeam(loginUser, mailId);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法同意邮件请求");
        }
        return ResultUtils.success(result);
    }
    // 拒绝某个邮件请求
    @PostMapping("/refuseJoinTeam")
    public BaseResponse<Boolean> refuseJoinTeam(@RequestParam Long mailId, HttpServletRequest httpServletRequest) {
        // 获取登录用户
        user loginUser = userService.getLoginUser(httpServletRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 调用MailService拒绝某个邮件请求
        boolean result = mailService.refuseJoinTeam(loginUser, mailId);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法拒绝邮件请求");
        }
        return ResultUtils.success(result);
    }
}
