package com.bxt.usercenter2.controller;

import com.bxt.usercenter2.common.BaseResponse;
import com.bxt.usercenter2.common.ErrorCode;
import com.bxt.usercenter2.common.ResultUtils;
import com.bxt.usercenter2.exception.BusinessException;
import com.bxt.usercenter2.model.domain.Team;
import com.bxt.usercenter2.service.TeamService;
import com.bxt.usercenter2.service.userService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.crypto.interfaces.PBEKey;

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
        if (result==null) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查找失败");
        return ResultUtils.success(result);
    }





}
