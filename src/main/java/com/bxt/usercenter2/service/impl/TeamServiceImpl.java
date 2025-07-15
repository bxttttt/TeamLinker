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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
        //对密码进行加密
        if (statusCode == StatusCode.SECRET) {
            String encodedPassword = passwordEncoder.encode(team.getPassword());
            team.setPassword(encodedPassword);
        }else{
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
    public Long joinTeamEveryoneCanJoin(long teamId, user loginUser) {
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
    public Long joinTeamEveryoneNeedAgreement(long teamId, user loginUser) {
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
    @Override
    public Long joinTeamEveryoneNeedPassword(long teamId, user loginUser, String pwd){
        Team team = checkTeamCanJoin(teamId, loginUser);
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不允许加入");
        }
        String encodedPassword = passwordEncoder.encode(pwd);
        System.out.println("Encoded Password: " + encodedPassword);
        if (team.getPassword() == null || !passwordEncoder.matches(pwd, team.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 直接加入队伍，不需要审核
        boolean save=addUserToTeam(loginUser.getId(), teamId);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败");
        }
        return teamId;
    }
    // 检测是普通成员退出队伍还是群主退出队伍的函数 如果是普通成员退出队伍 则返回0 如果是群主退出队伍 则返回1
    @Override
    public int checkUserExitTeam(long teamId, user loginUser) {
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
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查用户是否是群主
        if (team.getUserId().equals(loginUser.getId())) {
            return 1; // 群主退出队伍
        } else {
            return 0; // 普通成员退出队伍
        }
    }
    // 普通成员退出队伍函数
    @Override
    public boolean exitTeamAsMember(long teamId, user loginUser) {
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
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查用户是否在队伍中
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId)
                .eq("userId", loginUser.getId());
        boolean exists = userTeamService.exists(queryWrapper);
        if (!exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不在队伍中");
        }
        // 删除用户与队伍的关系
        boolean remove = userTeamService.remove(queryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
        }
        return remove; // 返回是否成功退出队伍
    }
    // 群主退出队伍函数
        // 如果队伍人数为1 则删除队伍 返回0
        // 如果队伍人数大于1 则先不删除队伍 返回（队伍人数-1）
    @Override
    public int exitTeamAsOwner(long teamId, user loginUser) {
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
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有群主才能退出队伍");
        }
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查队伍成员数量
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(queryWrapper);

        if (count <= 1) {
            // 队伍人数为1，删除队伍
            boolean remove = this.removeById(teamId);
            if (!remove) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
            }
            return 0; // 返回0表示队伍已被删除
        } else {
            // 队伍人数大于1，先不删除队伍
            return (int) count - 1; // 返回（队伍人数-1）
        }
    }

    // 前端如果收到的返回值大于等于1，则说明队伍里还有人，就让群主选择是解散队伍还是转让队伍
    // 群主解散队伍函数
    @Override
    public boolean dissolveTeam(long teamId, user loginUser) {
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
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有群主才能解散队伍");
        }
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 删除用户与队伍的关系
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean removeUserTeam = userTeamService.remove(queryWrapper);
        if (!removeUserTeam) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除用户与队伍关系失败");
        }
        // 删除队伍
        boolean removeTeam = this.removeById(teamId);
        if (!removeTeam) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
        }
        return true; // 返回是否成功解散队伍
    }
    // 群主转让队伍函数
    // 检查新群主是否已经创建了5个群
    // 转让队伍不包括把旧的群主从队伍中删除
    @Override
    public boolean transferTeamOwnership(long teamId, user loginUser, Long newOwnerId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        if (newOwnerId == null || newOwnerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新群主ID错误");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只有群主才能转让队伍");
        }
        if (team.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 检查新群主是否在队伍中
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId)
                .eq("userId", newOwnerId);
        boolean exists = userTeamService.exists(queryWrapper);
        if (!exists) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新群主不在队伍中");
        }
        // 检查新群主是否已经创建了5个群
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId", newOwnerId);
        long count = this.count(teamQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新群主已创建5个群，无法转让");
        }
        // 更新队伍的群主ID
        team.setUserId(newOwnerId);
        boolean update = this.updateById(team);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "转让队伍失败");
        }
        return true; // 返回是否成功转让队伍
    }
    // 获取当前当前队伍的所有用户
    @Override
    public List<user> getTeamUsers(long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeams = userTeamService.list(queryWrapper);
        if (userTeams.isEmpty()) {
            return List.of(); // 如果没有用户，返回空列表
        }
        List<Long> userIds = userTeams.stream()
                .map(UserTeam::getUserId)
                .toList();
        return userService.listByIds(userIds); // 批量查询用户信息
    }
    // 获取当前用户的所有创建的队伍
    @Override
    public List<Team> getCreatedTeams(user loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        return this.list(queryWrapper);
    }
    // 获取当前用户的所有加入的队伍，去掉用户创建的队伍
    @Override
    public List<Team> getJoinedTeams(user loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeams = userTeamService.list(queryWrapper);
        if (userTeams.isEmpty()) {
            return List.of(); // 如果没有加入的队伍，返回空列表
        }
        List<Long> teamIds = userTeams.stream()
                .map(UserTeam::getTeamId)
                .toList();
        List<Team> result= this.listByIds(teamIds); // 批量查询队伍信息
        // 过滤掉用户创建的队伍
        for (Team team : result) {
            if (team.getUserId().equals(loginUser.getId())) {
                result.remove(team);
            }
        }
        return result; // 批量查询队伍信息
    }










}




