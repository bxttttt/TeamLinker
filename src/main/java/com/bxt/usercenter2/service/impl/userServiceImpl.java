package com.bxt.usercenter2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.constant.userConstant;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.mapper.userMapper;
import com.bxt.usercenter2.service.userService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.bxt.usercenter2.model.domain.user;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.bxt.usercenter2.constant.userConstant.SALT;
import static com.bxt.usercenter2.constant.userConstant.USER_LOGIN_STATE;

/**
* @author bxt
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-05-11 01:24:33
*/
@Service
@Slf4j
public class userServiceImpl extends ServiceImpl<userMapper, user>
    implements userService{
    @Resource
    private userMapper userMapper;
//    public static final String USER_LOGIN_STATE="user_login_state";
    @Override
    public long userRegister(String userAccount, String userPwd, String checkPwd) {
        if (StringUtils.isAnyBlank(userAccount, userPwd, checkPwd)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        if (userPwd.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度小于8");
        }
        //账户不能包含特殊字符
        String validPattern="\\pP|\\pS|\\s+";
        Matcher matcher= Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        if (!userPwd.equals(checkPwd)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码与确认密码不一致");
        }
        // 查找重复账号 user_account
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        System.out.println("userAccount:"+userAccount);
        System.out.println("查询条件: " + queryWrapper.getCustomSqlSegment());
        long count = userMapper.selectCount(queryWrapper);
        System.out.println("count:"+count);
        System.out.println("this.count:"+this.count());
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        //对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPwd).getBytes());
//        System.out.println(encryptPassword);
        //插入数据
        user user=new user();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        boolean save_result=this.save(user);
        if (!save_result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"无法正常存储");
        }
        return user.getId();
    }

    @Override
    public user userLogin(String userAccount, String userPwd, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPwd)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPwd.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不能包含特殊字符
        String validPattern="\\pP|\\pS|\\s+";
        Matcher matcher= Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPwd).getBytes());
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("password", encryptPassword);
        System.out.println("查询条件: " + queryWrapper.getCustomSqlSegment());
        user user = userMapper.selectOne(queryWrapper);
        if (user == null){
            log.info("log in failed");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //用户脱敏，否则前段能够看到所有返回的用户信息

        //记录用户登录态，将其存到服务器上cookie
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        //返回脱敏后的用户信息
        return getSafeUser(user);

    }
    @Override
    public user getSafeUser(user user){
        //用户脱敏，否则前段能够看到所有返回的用户信息
        user safetyUser=new user();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setTags(user.getTags());
        //返回脱敏后的用户信息
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {

        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 0;
    }

    @Override
    public List<user> searchUsersByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<user> queryWrapper=new QueryWrapper<>();
        for (String tagName:tagNameList){
            queryWrapper.like("tags",tagName);
        }
        List<user> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());

    }

    @Override
    public Integer updateUser(user user,HttpServletRequest httpServletRequest) {
        user currentUser= (com.bxt.usercenter2.model.domain.user) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
//        if (currentUser==null) throw new BusinessException(ErrorCode.NOT_LOGIN);
//        long id=user.getId();
//        return Math.toIntExact(currentUser.getId());
//        if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        return userMapper.updateById(user);
        if (currentUser.getUserRole()==1|| Objects.equals(currentUser.getId(), user.getId())){
            long id=user.getId();
            if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
            return userMapper.updateById(user);
        }else throw new BusinessException(ErrorCode.NO_AUTH);

    }

    @Override
    public user getLoginUser(HttpServletRequest request) {
        Object userObj=request.getSession().getAttribute(userConstant.USER_LOGIN_STATE);
        user user=(user) userObj;
        if (user==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        return user;
    }


    @Override
    public Page<user> recommendUsersPagedResultThroughRequest(HttpServletRequest request, @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        Page<user> userPage = new Page<>(page, pageSize);
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        Page<user> pagedResult = page(userPage, queryWrapper);

        List<user> rawUserList = pagedResult.getRecords();

        List<user> safeUserList = rawUserList.stream()
                .map(this::getSafeUser)
                .collect(Collectors.toList());
        pagedResult.setRecords(safeUserList);

        // 打印调试信息
        System.out.println("分页参数 page = " + page + ", pageSize = " + pageSize);
        System.out.println("总记录数 = " + pagedResult.getTotal());
        System.out.println("当前页返回条数 = " + rawUserList.size());
        System.out.println("脱敏后返回条数 = " + safeUserList.size());
        System.out.println("页数 = " + pagedResult.getPages());
        return pagedResult;
    }

    @Override
    public Page<user> recommendUsersPagedResultThroughUser(user user, @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        Page<user> userPage = new Page<>(page, pageSize);
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        Page<user> pagedResult = page(userPage, queryWrapper);

        List<user> rawUserList = pagedResult.getRecords();

        List<user> safeUserList = rawUserList.stream()
                .map(this::getSafeUser)
                .collect(Collectors.toList());
        pagedResult.setRecords(safeUserList);

        // 打印调试信息
        System.out.println("分页参数 page = " + page + ", pageSize = " + pageSize);
        System.out.println("总记录数 = " + pagedResult.getTotal());
        System.out.println("当前页返回条数 = " + rawUserList.size());
        System.out.println("脱敏后返回条数 = " + safeUserList.size());
        System.out.println("页数 = " + pagedResult.getPages());
        return pagedResult;
    }

}




