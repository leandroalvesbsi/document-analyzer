package com.visiblethread.documentanalyzer.userteam;

import com.visiblethread.documentanalyzer.team.Team;
import com.visiblethread.documentanalyzer.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_team")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTeamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email") // Changed from "email"
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "active")
    private boolean active;

}