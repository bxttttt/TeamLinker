package com.bxt.usercenter2.controller;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
import com.bxt.usercenter2.dto.PageRequest;
import com.bxt.usercenter2.dto.TeamAddRequest;
import com.bxt.usercenter2.dto.TeamQuery;
import com.bxt.usercenter2.enums.StatusCode;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.model.domain.user;
import com.bxt.usercenter2.service.TeamService;
import com.bxt.usercenter2.service.userService;
import com.bxt.usercenter2.vo.TeamUserVO;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.interfaces.PBEKey;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/Team")
public class TeamController {
    @Resource
    private userService userService;

    @Resource
    private TeamService teamService;

    @PostMapping("/create")
    public BaseResponse<Long> createTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"参数复制失败");
        }

        team.setCreateTime(new Date());
        long result=teamService.createTeam(team, userService.getLoginUser(request));
        if (result<=0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加失败");
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/join")
    public BaseResponse<Long> joinTeam(Long teamId, String pwd,HttpServletRequest request){
        if (teamId==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (teamId<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Team team = teamService.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        long result=-1;
        if (team.getStatus()== StatusCode.PUBLIC_AND_EVERYONE.getCode()){
            result=teamService.joinTeamEveryoneCanJoin(teamId, userService.getLoginUser(request));
        }
        if (team.getStatus()==StatusCode.PUBLIC_AND_LIMITED.getCode()){
            result=teamService.joinTeamEveryoneNeedAgreement(teamId, userService.getLoginUser(request));
        }
        if (team.getStatus()==StatusCode.SECRET.getCode()){
            result=teamService.joinTeamEveryoneNeedPassword(teamId, userService.getLoginUser(request), pwd);
        }
        if (result<=0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"加入失败");
        }
        return ResultUtils.success(result);

    }

//    @PostMapping("/delete")
//    public BaseResponse<Long> deleteTeam(@RequestBody long id){
//        if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
//        boolean result=teamService.removeById(id);
//        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
//        return ResultUtils.success(id);
//    }

    @PostMapping("/update")
    public BaseResponse<Long> updateTeam(@RequestBody Team team){
        if (team==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean result=teamService.updateById(team);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        return ResultUtils.success(team.getId());
    }

//    @GetMapping("/get")
//    public BaseResponse<Team> getTeamById(@RequestParam long id){
//        if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
//        Team result=teamService.getById(id);
//        if (result==null) throw new BusinessException(ErrorCode.NULL_ERROR,"查找失败");
//        return ResultUtils.success(result);
//    }
    @GetMapping("/searchTeam/ById")
    public BaseResponse<TeamUserVO> searchTeamById(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        TeamUserVO teamUserVO = teamService.searchTeamThroughId(teamId, userService.getLoginUser(request));
        if (teamUserVO == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(teamUserVO);
    }
    @GetMapping("/searchTeam/ByName")
    public BaseResponse<Page<TeamUserVO>> searchTeamByName(@RequestParam String teamName,
                                                            PageRequest pageRequest,
                                                            HttpServletRequest request) {
        if (StringUtils.isBlank(teamName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不能为空");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        Page<TeamUserVO> teamPage = teamService.searchTeamByName(teamName, userService.getLoginUser(request), pageRequest.getCurrent(), pageRequest.getPageSize());
        if (teamPage == null || teamPage.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到符合条件的队伍");
        }
        return ResultUtils.success(teamPage);
    }
    // 显示用户加入的队伍
    @GetMapping("/myJoinedTeams")
    public BaseResponse<List<Team>> myJoinedTeams(PageRequest pageRequest, HttpServletRequest request) {
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        List<Team> teamPage = teamService.getJoinedTeams(userService.getLoginUser(request));
        if (teamPage == null || teamPage.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有加入的队伍");
        }
        return ResultUtils.success(teamPage);
    }
    // 显示用户创建的队伍
    @GetMapping("/myCreatedTeams")
    public BaseResponse<List<Team>> myCreatedTeams(PageRequest pageRequest, HttpServletRequest request) {
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        List<Team> teamPage = teamService.getCreatedTeams(userService.getLoginUser(request));
        if (teamPage == null || teamPage.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有创建的队伍");
        }
        return ResultUtils.success(teamPage);
    }
    // 退出队伍，返回是普通成员还是群主
    // 1-群主退出 0-普通成员退出
    @PostMapping("/exit")
    public BaseResponse<Integer> exitTeam(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        int result = teamService.checkUserExitTeam(teamId, userService.getLoginUser(request));

        return ResultUtils.success(result);
    }
    // 普通成员退出队伍
    @PostMapping("/exit/Member")
    public BaseResponse<Boolean> exitTeamMember(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = teamService.exitTeamAsMember(teamId, userService.getLoginUser(request));
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
        }
        return ResultUtils.success(true);
    }
    // 群主退出队伍，返回队伍人数
    @PostMapping("/exit/Dissolve")
    public BaseResponse<Integer> exitTeamDissolve(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        int result = teamService.exitTeamAsOwner(teamId, userService.getLoginUser(request));
        return ResultUtils.success(result);
    }
    // 群主解散队伍
    @PostMapping("/exit/dissolve")
    public BaseResponse<Boolean> dissolveTeam(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = teamService.dissolveTeam(teamId, userService.getLoginUser(request));
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散队伍失败");
        }
        return ResultUtils.success(true);
    }
    // 群主转让队伍再退出
    @PostMapping("/exit/transfer")
    public BaseResponse<Boolean> exitAndTransferTeam(@RequestParam long teamId, @RequestParam long newOwnerId, HttpServletRequest request) {
        if (teamId <= 0 || newOwnerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID或新群主ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = teamService.transferTeamOwnership(teamId, userService.getLoginUser(request), newOwnerId);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "转让队伍失败");
        }
        // 退出队伍
        boolean exitResult = teamService.exitTeamAsMember(teamId, userService.getLoginUser(request));
        return ResultUtils.success(true);
    }
    // 群主转让队伍
    @PostMapping("/transfer")
    public BaseResponse<Boolean> transferTeam(@RequestParam long teamId, @RequestParam long newOwnerId, HttpServletRequest request) {
        if (teamId <= 0 || newOwnerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID或新群主ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = teamService.transferTeamOwnership(teamId, userService.getLoginUser(request),newOwnerId);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "转让队伍失败");
        }
        return ResultUtils.success(true);
    }
    // 返回一个队伍的所有成员
    @GetMapping("/members")
    public BaseResponse<List<user>> getTeamMembers(@RequestParam long teamId, HttpServletRequest request) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍ID错误");
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        List<user> members = teamService.getTeamUsers(teamId);
        if (members == null || members.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到队伍成员");
        }
        return ResultUtils.success(members);
    }



//    @GetMapping("/list")
//    public BaseResponse<List<Team>> listTeams(@RequestBody TeamQuery teamQuery) {
//        if (teamQuery == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
//        }
//
//        Team team = new Team();
//        try {
//            BeanUtils.copyProperties(team, teamQuery);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数复制失败");
//        }
//
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//        // 如果你只想对 name 模糊查询，可单独处理
//        if (StringUtils.isNotBlank(teamQuery.getName())) {
//            queryWrapper.like("name", teamQuery.getName());
//        }
//
//        List<Team> result = teamService.list(queryWrapper);
//
//        if (result == null || result.isEmpty()) {
//            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到符合条件的队伍");
//        }
//
//        return ResultUtils.success(result);
//    }
//    @GetMapping("/list/page")
//    public BaseResponse<Page<Team>> listTeamsByPage(@RequestBody TeamQuery teamQuery, PageRequest pageRequest) {
//        if (teamQuery == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
//        }
//
//        Team team = new Team();
//        try {
//            BeanUtils.copyProperties(team, teamQuery);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数复制失败");
//        }
//
////        long current = teamQuery.getCurrent() != null ? teamQuery.getCurrent() : 1;
////        long pageSize = teamQuery.getPageSize() != null ? teamQuery.getPageSize() : 10;
//
//        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
//
//        if (StringUtils.isNotBlank(teamQuery.getName())) {
//            queryWrapper.like("name", teamQuery.getName());
//        }
//
//        Page<Team> teamPage = teamService.page(new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()), queryWrapper);
//
//        return ResultUtils.success(teamPage);
//    }







}
