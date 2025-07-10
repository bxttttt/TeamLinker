package com.bxt.usercenter2.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.model.domain.user;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author bxt
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-05-11 01:24:33
*/

public interface userService extends IService<user> {
    /** 用户注册
     *
     * @param userAccount 用户账号
     * @param userPwd 用户密码
     * @param checkPwd 确认密码
     * @return id
     */
    long userRegister(String userAccount, String userPwd, String checkPwd);

    /**
     *
     * @param userAccount 用户账号
     * @param userPwd 用户密码
     * @return 脱敏后的用户信息
     */
    user userLogin(String userAccount, String userPwd, HttpServletRequest request);


    user getSafeUser(user user);

    int userLogout(HttpServletRequest request);

    List<user> searchUsersByTags(List<String> tagList);

    Integer updateUser(user user,HttpServletRequest httpServletRequest);

    user getLoginUser(HttpServletRequest request);



    Page<user> recommendUsersPagedResultThroughRequest(HttpServletRequest request, @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int pageSize);

    Page<user> recommendUsersPagedResultThroughUser(user user, @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize);
}
