package com.visiblethread.documentanalyzer.userteam;

import com.visiblethread.documentanalyzer.team.Team;
import com.visiblethread.documentanalyzer.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {
    List<UserTeam> findByUser_EmailOrderByStartDateAsc(String email);
    List<UserTeam> findByUser_EmailAndActiveTrue(String email);
    Optional<UserTeam> findByUserAndTeamAndActiveTrue(User user, Team team);
}