package com.bxt.usercenter2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.enums.StatusCode;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.mapper.TeamMapper;
import com.bxt.usercenter2.model.domain.Mail;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.UserTeam;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.MailService;
import com.bxt.usercenter2.service.TeamService;

import com.bxt.usercenter2.service.UserTeamService;
import com.bxt.usercenter2.service.userService;
import com.bxt.usercenter2.vo.TeamUserVO;
import jakarta.annotation.Resource;
import org.springdoc.core.converters.ResponseSupportConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    public long createTeam(Team team, user loginUser) {
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
            status = 0; // 默认状态为公开 任何人可以加入
        } else {
            status = team.getStatus();
        }
        if (status < 0 || status > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态码错误");
        }
        StatusCode statusCode = StatusCode.getTypeByValue(status);
        if (statusCode == StatusCode.SECRET&& team.getPassword().length()==0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍需要设置密码");
        }
        if (statusCode == StatusCode.SECRET && team.getPassword().length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍密码不能超过20个字符");
        }
        if (statusCode!=StatusCode.SECRET) {
            team.setPassword(null); // 非加密队伍不需要密码
        }
//        System.out.println("队伍状态：" + team.getStatus());
        if (team.getExpireTime() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能为空");
        }

        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能小于当前时间");
        }
        long userId = loginUser.getId();
        team.setUserId(userId);
        /*
        一个用户只能创建5个队伍
         */
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long count = this.count(queryWrapper);
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
    @Autowired
    userService userService;

    @Override
    public TeamUserVO searchTeamThroughId(long teamId, user loginUser) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        StatusCode statusCode = StatusCode.getTypeByValue(team.getStatus());
        if (statusCode==StatusCode.PRIVATE){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私有队伍无法查看");
        }

        return buildTeamUserVO(team, loginUser);
    }


    @Override
    public Page<TeamUserVO> searchTeamByName(String teamName, user loginUser, long pageNum, long pageSize) {
        if (StringUtils.isBlank(teamName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        // 构建分页查询
        Page<Team> teamPage = new Page<>(pageNum, pageSize);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", teamName)
                .in("status", StatusCode.PUBLIC_AND_EVERYONE.getCode(),
                        StatusCode.SECRET.getCode(),
                        StatusCode.PUBLIC_AND_LIMITED.getCode());

        Page<Team> pageResult = this.page(teamPage, queryWrapper);

        // 构造VO列表
//        System.out.println("查询到的队伍数量：" + pageResult.getTotal());
//        System.out.println(pageResult);
        List<TeamUserVO> teamUserVOList = pageResult.getRecords().stream()
                .map(team -> buildTeamUserVO(team, loginUser))
                .toList();

        // 返回分页对象
        Page<TeamUserVO> resultPage = new Page<>();
        resultPage.setCurrent(pageResult.getCurrent());
        resultPage.setSize(pageResult.getSize());
        resultPage.setTotal(pageResult.getTotal());
        resultPage.setRecords(teamUserVOList);
        return resultPage;
    }
    @Resource
    private TeamMapper teamMapper;
    @Override
    public TeamUserVO buildTeamUserVO(Team team, user loginUser) {
        TeamUserVO vo = new TeamUserVO();
        vo.setTeamId(team.getId());
        vo.setTeamName(team.getName());
        vo.setTeamDescription(team.getDescription());
        vo.setTeamMaxNum(team.getMaxNum());
        vo.setTeamExpireTime(team.getExpireTime());
        vo.setStatus(team.getStatus());
        vo.setUserId(team.getUserId());
        vo.setUserAvatar(loginUser.getAvatarUrl());

        // 查询当前队伍所有成员
        List<UserTeam> userTeamList = userTeamService.list(
                new QueryWrapper<UserTeam>().eq("teamId", team.getId()));
        List<Long> userIdList = userTeamList.stream()
                .map(UserTeam::getUserId).toList();
        vo.setUserIdList(userIdList);
        vo.setSumPeople(userIdList.size());

        // 批量获取头像，避免 N 次数据库调用
        List<String> avatarList = userIdList.stream()
                .map(userService::getById)
                .filter(Objects::nonNull)
                .map(user::getAvatarUrl)
                .toList();
        vo.setUserAvatarList(avatarList);

        return vo;
    }
    @Override
    public long getSumPeople(Team team) {
        if (team == null || team.getId() == null) {
            return 0;
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", team.getId());
        return userTeamService.count(queryWrapper);
    }
    //检查team能不能加入，封装成一个函数
    @Override
    public Team checkTeamCanJoin(long teamId, user loginUser) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        StatusCode statusCode = StatusCode.getTypeByValue(team.getStatus());
        if (statusCode == StatusCode.PRIVATE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私有队伍无法加入");
        }
        if (team.getMaxNum() <= getSumPeople(team)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        // 检查队伍是否过期
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查用户是否已经在队伍中
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId)
                .eq("userId", loginUser.getId());
        boolean exists = userTeamService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已在队伍中");
        }
        return team;
    }
    @Override
    public Long joinTeamStatus0(long teamId, user loginUser) {
        Team team = checkTeamCanJoin(teamId, loginUser);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不允许加入");
        }

        // 直接加入队伍，不需要审核
        boolean save=addUserToTeam(loginUser.getId(), teamId);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败");
        }
        return teamId;
    }
    @Override
    public boolean addUserToTeam(Long userId, Long teamId) {
        if (userId == null || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID或队伍ID不能为空");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (team.getMaxNum() <= getSumPeople(team)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(team.getCreateTime());
        return userTeamService.save(userTeam);
    }
    @Autowired
    @Lazy
    private MailService mailService;
    @Override
    public Long joinTeamStatus1(long teamId, user loginUser) {
        Team team = checkTeamCanJoin(teamId, loginUser);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不允许加入");
        }
        // 发送验证消息
        Mail mail = new Mail();
        mail.setRelatedTeam(teamId);
        mail.setSendUserId(loginUser.getId());
        mail.setReceiveUserId(team.getUserId());
        mail.setMessage("用户 " + loginUser.getUserAccount() + " 请求加入队伍 " + team.getName() + "，请确认。");
        mail.setHaveRead(0); // 0表示未处理
        mail.setMailType(1); // 1表示加入队伍请求
        QueryWrapper<Mail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("relatedTeam", teamId)
                .eq("sendUserId", loginUser.getId())
                .eq("receiveUserId", team.getUserId())
                .eq("haveRead", 0);
        long count = mailService.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已发送加入请求，请等待处理");
        }
        boolean saveMail = mailService.save(mail);
        if (!saveMail) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送加入请求失败");
        }


        return teamId;
    }






}




