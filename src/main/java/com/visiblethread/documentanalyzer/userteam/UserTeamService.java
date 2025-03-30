package com.visiblethread.documentanalyzer.userteam;

import com.visiblethread.documentanalyzer.team.Team;
import com.visiblethread.documentanalyzer.team.TeamRepository;
import com.visiblethread.documentanalyzer.user.User;
import com.visiblethread.documentanalyzer.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserTeamService {

    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public UserTeamService(UserTeamRepository userTeamRepository, UserRepository userRepository, TeamRepository teamRepository) {
        this.userTeamRepository = userTeamRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public User assignNewUserToTeam(User user, Long teamId, LocalDateTime startDate) {

        Team teamById = getTeamById(teamId);

        if(this.userRepository.findByEmailAndActiveIsTrue(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already exists");
        }

        user.setActive(true);

        return addUserToTeam(userRepository.saveAndFlush(user), teamById, startDate).getUser();

    }

    public User assignExistingUserToTeam(String userEmail, Long teamId, LocalDateTime startDate) {

        Team team = getTeamById(teamId);
        User user = getUserByEmail(userEmail);

        checkExistingUserInTeam(user, team);

        return addUserToTeam(user, team, startDate).getUser();

    }

    @Transactional
    public UserTeam changeUserTeam(String userEmail, Long fromTeamId, Long toTeamId, LocalDateTime startDate) {

        User user = getUserByEmail(userEmail);
        Team fromTeam = getTeamById(fromTeamId);
        Team toTeam = getTeamById(toTeamId);

        checkExistingUserInTeam(user, toTeam);

        removeUserFromTeam(user, fromTeam);

        return addUserToTeam(user, toTeam, startDate);

    }

    @Transactional
    public void removeUserFromTeam(String userEmail, Long teamId) {

        User user = getUserByEmail(userEmail);

        Team team = getTeamById(teamId);

        removeUserFromTeam(user, team);

        if (userTeamRepository.findByUser_EmailAndActiveTrue(userEmail).isEmpty()) {
            user.setActive(false);
            userRepository.saveAndFlush(user);
        }

    }

    public List<UserTeam> getTeamHistory(String email) {
        return userTeamRepository.findByUser_EmailOrderByStartDateAsc(email);
    }

    public void checkExistingUserInTeam(User user, Team team) {
        userTeamRepository.findByUserAndTeamAndActiveTrue(user, team)
                .ifPresent(ut -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already active in this team");
                });
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndActiveIsTrue(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    private UserTeam addUserToTeam(User user, Team team, LocalDateTime startDate) {

        checkExistingUserInTeam(user, team);

        UserTeam userTeam = UserTeam.builder()
                .user(user)
                .team(team)
                .startDate(startDate)
                .active(true)
                .build();

        return userTeamRepository.saveAndFlush(userTeam);

    }

    private UserTeam removeUserFromTeam(User user, Team team) {
        UserTeam userTeam = userTeamRepository.findByUserAndTeamAndActiveTrue(user, team)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not active in this team"));
        userTeam.setActive(false);
        userTeam.setEndDate(LocalDateTime.now());
        return userTeamRepository.saveAndFlush(userTeam);
    }

}