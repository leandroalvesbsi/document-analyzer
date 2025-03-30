package com.visiblethread.documentanalyzer.userteam;

import com.visiblethread.documentanalyzer.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user-team")
public class UserTeamController {

    private UserTeamService teamService;

    public UserTeamController(UserTeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/team/{teamId}")
    public User assignNewUserToTeam(@PathVariable Long teamId, @RequestBody User user, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate) {
        return teamService.assignNewUserToTeam(user, teamId, startDate);
    }

    @PostMapping("/{email}/from-team/{fromTeamId}/to-team/{toTeamId}")
    public UserTeam changeUserTeam(@PathVariable String email, @PathVariable Long fromTeamId, @PathVariable Long toTeamId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate) {
        return teamService.changeUserTeam(email, fromTeamId, toTeamId, startDate);
    }

    @PostMapping("/user/{userEmail}/team/{teamId}")
    public User assignExistingUserToTeam(@PathVariable String userEmail, @PathVariable Long teamId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate) {
        return teamService.assignExistingUserToTeam(userEmail, teamId, startDate);
    }

    @PostMapping("/{email}/team/{teamId}")
    public void removeUserFromTeam(@PathVariable String email, @PathVariable Long teamId) {
        teamService.removeUserFromTeam(email, teamId);
    }

    @GetMapping("/{email}/history")
    public List<UserTeam> getTeamHistory(@PathVariable String email) {
        return teamService.getTeamHistory(email);
    }

}