package com.visiblethread.documentanalyzer.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String email;

    @Column(name = "active")
    private boolean active;

    @CreatedDate
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

}