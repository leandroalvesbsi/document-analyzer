package com.visiblethread.documentanalyzer.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailAndActiveIsTrue(String email);

    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "LEFT JOIN Document d ON d.user.email = u.email " +
            "AND d.uploadTimestamp BETWEEN :startDate AND :endDate " +
            "WHERE u.active = true " +
            "AND u.registrationDate <= :endDate " +
            "AND d.id IS NULL")
    long countUsersWithoutDocumentsInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}