package com.bxt.usercenter2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.model.request.userLoginRequest;
import com.bxt.usercenter2.model.request.userRegisterRequest;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.userService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.bxt.usercenter2.constant.userConstant;


@RestController
@RequestMapping("/user")
@Tag(name = "用户")
public class userController {
    @Resource
    private userService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody userRegisterRequest userRegisterRequest){
        if (userRegisterRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount=userRegisterRequest.getUserAccount();
        String userPwd=userRegisterRequest.getUserPwd();
        String checkPwd=userRegisterRequest.getCheckPwd();
        if (StringUtils.isAnyBlank(userAccount,userPwd,checkPwd)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Long result= userService.userRegister(userAccount,userPwd,checkPwd);
        return ResultUtils.success(result);
    }
    @Operation(
            summary = "用户登录接口",
            description = "通过账号和密码进行登录，成功后返回用户信息"
    )
    @Parameters({
            @Parameter(name = "userLoginRequest", description = "用户登录请求体，包含账号和密码", required = true)
    })
    @PostMapping("/login")
    public BaseResponse<user> userLogin(@RequestBody userLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        if (userLoginRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userAccount=userLoginRequest.getUserAccount();
        String userPwd=userLoginRequest.getUserPwd();
        if (StringUtils.isAnyBlank(userAccount,userPwd)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//            return ResultUtils.params_error();
        }
        user result=userService.userLogin(userAccount,userPwd,httpServletRequest);
        return ResultUtils.success(result);
    }
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody user user,HttpServletRequest httpServletRequest){
        if (user==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Integer result= userService.updateUser(user,httpServletRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest httpServletRequest){
        if (httpServletRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        int result= userService.userLogout(httpServletRequest);
        return ResultUtils.success(result);
    }
    @GetMapping("/recommendWithoutRedis")
    public BaseResponse<Page<user>> recommendUsersWithoutRedis(HttpServletRequest request, int page, int pageSize){
        user loginUser = userService.getLoginUser(request);
        Page<user> pagedResult = userService.recommendUsersPagedResultThroughRequest(request,page,pageSize);
        // 返回统一格式的响应
        return ResultUtils.success(pagedResult);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<user>> recommendUsers(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        user loginUser = userService.getLoginUser(request);

        // Redis 缓存 key
        String redisKey = String.format("bxt:user:recommend:%s:page:%d:size:%d", loginUser.getId(), page, pageSize);

        // 先查缓存
        Page<user> cachedPage = (Page<user>) redisTemplate.opsForValue().get(redisKey);
        if (cachedPage != null) {
            System.out.println("从 Redis 中读取缓存结果");
            return ResultUtils.success(cachedPage);
        }

        // 没有缓存则查询数据库
        Page<user> pagedResult = userService.recommendUsersPagedResultThroughRequest(request,page,pageSize);

        // 缓存结果，设置过期时间 5 分钟
        redisTemplate.opsForValue().set(redisKey, pagedResult, 5, TimeUnit.MINUTES);

        return ResultUtils.success(pagedResult);
    }




    @GetMapping("/search")
    public BaseResponse<List<user>> userSearch(String username,HttpServletRequest request){
        boolean adminOrNot=isAdmin(request);
        if (!adminOrNot){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员");
        }
        QueryWrapper<user> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        List<user> userList= userService.list(queryWrapper);
        List<user> result= userList.stream().map(user -> {
            return userService.getSafeUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<user>> searchUsersByTags(@RequestParam List<String> tags){
        if (CollectionUtils.isEmpty(tags)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<user> result = userService.searchUsersByTags(tags);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> userDelete(long id,HttpServletRequest request){
        if (id<0||!isAdmin(request)) throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员");
        Boolean result= userService.removeById(id);
        return ResultUtils.success(result);
    }

    private boolean isAdmin(HttpServletRequest request){
//        Object userObj=request.getSession().getAttribute(userConstant.USER_LOGIN_STATE);
//        user user=(user) userObj;
        user user=userService.getLoginUser(request);
        if (user==null||user.getUserRole()!=userConstant.ADMIN_ROLE) return false;
        return true;
    }




}

