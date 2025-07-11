package com.bxt.usercenter2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
import com.bxt.usercenter2.dto.PageRequest;
import com.bxt.usercenter2.dto.TeamQuery;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.service.TeamService;
import com.bxt.usercenter2.service.userService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.interfaces.PBEKey;
import java.util.List;

@RestController
@RequestMapping("/Team")
public class TeamController {
    @Resource
    private userService userService;

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team){
        if (team==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean save=teamService.save(team);
        if (!save) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"插入失败");
        return ResultUtils.success(team.getId());
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

    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(@RequestBody TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }

        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数复制失败");
        }

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        // 如果你只想对 name 模糊查询，可单独处理
        if (StringUtils.isNotBlank(teamQuery.getName())) {
            queryWrapper.like("name", teamQuery.getName());
        }

        List<Team> result = teamService.list(queryWrapper);

        if (result == null || result.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有找到符合条件的队伍");
        }

        return ResultUtils.success(result);
    }
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(@RequestBody TeamQuery teamQuery, PageRequest pageRequest) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询参数为空");
        }

        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数复制失败");
        }

//        long current = teamQuery.getCurrent() != null ? teamQuery.getCurrent() : 1;
//        long pageSize = teamQuery.getPageSize() != null ? teamQuery.getPageSize() : 10;

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);

        if (StringUtils.isNotBlank(teamQuery.getName())) {
            queryWrapper.like("name", teamQuery.getName());
        }

        Page<Team> teamPage = teamService.page(new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()), queryWrapper);

        return ResultUtils.success(teamPage);
    }







}
