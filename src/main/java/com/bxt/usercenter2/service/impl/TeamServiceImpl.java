package com.bxt.usercenter2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.enums.StatusCode;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.mapper.TeamMapper;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.UserTeam;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.TeamService;

import com.bxt.usercenter2.service.UserTeamService;
import org.springdoc.core.converters.ResponseSupportConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.jta.UserTransactionAdapter;

/**
* @author bxt
* @description 针对表【team】的数据库操作Service实现
* @createDate 2025-06-26 15:58:52
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    private final ResponseSupportConverter responseSupportConverter;
    private final UserTeamService userTeamService;


    public TeamServiceImpl(ResponseSupportConverter responseSupportConverter, UserTeamService userTeamService) {
        this.responseSupportConverter = responseSupportConverter;
        this.userTeamService = userTeamService;
    }

    @Override
    public long addTeam(Team team, user loginUser) {
        System.out.println("添加队伍");
        if (team == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        if (team.getMaxNum()<= 0 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不能小于等于0");
        }
        if (team.getMaxNum() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不能大于20");
        }
        if (team.getName() == null || team.getName().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
        }
        if (team.getName().length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能超过20个字符");
        }
        if (team.getDescription()!=null && team.getDescription().length()>200){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能超过200个字符");
        }
//        System.out.println("队伍状态：" + team.getStatus());
        int status;
        if (team.getStatus() == null) {
            status = 0; // 默认状态为公开
        } else {
            status = team.getStatus();
        }
        if (status < 0 || status > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态码错误");
        }
        StatusCode statusCode = StatusCode.getTypeByValue(status);
        if (statusCode == StatusCode.SECRET&& team.getPassword().length()==0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私密队伍需要设置密码");
        }
        if (statusCode == StatusCode.SECRET && team.getPassword().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私密队伍密码不能超过20个字符");
        }
        if (statusCode!=StatusCode.SECRET) {
            team.setPassword(null); // 非私密队伍不需要密码
        }
//        System.out.println("队伍状态：" + team.getStatus());
        if (team.getExpireTime() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能为空");
        }

        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能小于当前时间");
        }
        long userId = loginUser.getId();
//        team.setUserId(userId);
        /*
        一个用户只能创建5个队伍
         */
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long count = userTeamService.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "一个用户只能创建5个队伍");
        }
//        System.out.println("用户ID：" + userId);
        boolean save = this.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(team.getCreateTime());
        save= userTeamService.save(userTeam);
//        System.out.println("用户队伍关系保存结果：" + save);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入用户队伍关系失败");
        }
        // 返回新创建的队伍ID
        if (team.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍ID不能为空");
        }
        return team.getId();

    }
}




