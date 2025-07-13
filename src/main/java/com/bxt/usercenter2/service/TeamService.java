package com.bxt.usercenter2.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.vo.TeamUserVO;


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

    Long joinTeamStatus0(long teamId, user loginUser);

    Long joinTeamStatus1(long teamId, user loginUser, String password);
}
