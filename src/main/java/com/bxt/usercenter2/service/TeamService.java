package com.bxt.usercenter2.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.vo.TeamUserVO;

import java.util.List;


/**
* @author bxt
* @description 针对表【team】的数据库操作Service
* @createDate 2025-06-26 15:58:52
*/
public interface TeamService extends IService<Team> {
    TeamUserVO buildTeamUserVO(Team team, user loginUser);

    /*
        * 创建队伍
     */
    long createTeam(Team team, user loginUser);

    TeamUserVO searchTeamThroughId(long teamId, user loginUser);

    Page<TeamUserVO> searchTeamByName(String teamName, user loginUser, long pageNum, long pageSize);

    long getSumPeople(Team team);


    //检查team能不能加入，封装成一个函数
    Team checkTeamCanJoin(long teamId, user loginUser);

    Long joinTeamEveryoneCanJoin(long teamId, user loginUser);

    boolean addUserToTeam(Long userId, Long teamId);

    Long joinTeamEveryoneNeedAgreement(long teamId, user loginUser);

    Long joinTeamEveryoneNeedPassword(long teamId, user loginUser,String pwd);

    // 检测是普通成员退出队伍还是群主退出队伍的函数 如果是普通成员退出队伍 则返回0 如果是群主退出队伍 则返回1
    int checkUserExitTeam(long teamId, user loginUser);

    // 普通成员退出队伍函数
    boolean exitTeamAsMember(long teamId, user loginUser);

    // 群主退出队伍函数
        // 如果队伍人数为1 则删除队伍 返回0
        // 如果队伍人数大于1 则先不删除队伍 返回（队伍人数-1）
    int exitTeamAsOwner(long teamId, user loginUser);

    // 前端如果收到的返回值大于等于1，则说明队伍里还有人，就让群主选择是解散队伍还是转让队伍
    // 群主解散队伍函数
    boolean dissolveTeam(long teamId, user loginUser);

    // 群主转让队伍函数
    // 检查新群主是否已经创建了5个群
    boolean transferTeamOwnership(long teamId, user loginUser, Long newOwnerId);

    // 获取当前当前队伍的所有用户
    List<user> getTeamUsers(long teamId);

    // 获取当前用户的所有创建的队伍
    List<Team> getCreatedTeams(user loginUser);

    // 获取当前用户的所有加入的队伍，去掉用户创建的队伍
    List<Team> getJoinedTeams(user loginUser);
}
