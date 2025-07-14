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
import com.bxt.usercenter2.service.TeamService;
import com.bxt.usercenter2.service.userService;
import com.bxt.usercenter2.vo.TeamUserVO;
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
    public BaseResponse<Long> joinTeam(Long teamId, HttpServletRequest request){
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
            result=teamService.joinTeamStatus0(teamId, userService.getLoginUser(request));
        }
        if (team.getStatus()==StatusCode.PUBLIC_AND_LIMITED.getCode()){
            result=teamService.joinTeamStatus1(teamId, userService.getLoginUser(request));
        }
        if (result<=0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"加入失败");
        }
        return ResultUtils.success(result);

    }

    @PostMapping("/delete")
    public BaseResponse<Long> deleteTeam(@RequestBody long id){
        if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        boolean result=teamService.removeById(id);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        return ResultUtils.success(id);
    }

    @PostMapping("/update")
    public BaseResponse<Long> updateTeam(@RequestBody Team team){
        if (team==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean result=teamService.updateById(team);
        if (!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        return ResultUtils.success(team.getId());
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam long id){
        if (id<=0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        Team result=teamService.getById(id);
        if (result==null) throw new BusinessException(ErrorCode.NULL_ERROR,"查找失败");
        return ResultUtils.success(result);
    }
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
