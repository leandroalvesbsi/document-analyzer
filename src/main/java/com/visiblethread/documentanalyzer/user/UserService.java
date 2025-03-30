package com.visiblethread.documentanalyzer.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public long countUsersWithoutDocumentsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countUsersWithoutDocumentsInPeriod(startDate, endDate);
    }

}