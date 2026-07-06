package com.example.todolist.config;

import com.example.todolist.model.Role;
import com.example.todolist.model.User;
import com.example.todolist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default Leader account on first startup so there is always a way
 * to log in and start creating Employee accounts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.leader-username}")
    private String leaderUsername;

    @Value("${app.seed.leader-password}")
    private String leaderPassword;

    @Value("${app.seed.leader-fullname}")
    private String leaderFullName;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(leaderUsername)) {
            return;
        }

        User leader = User.builder()
                .username(leaderUsername)
                .password(passwordEncoder.encode(leaderPassword))
                .fullName(leaderFullName)
                .role(Role.LEADER)
                .build();

        userRepository.save(leader);
        log.info("Đã tạo tài khoản Leader mặc định: username='{}'", leaderUsername);
    }
}
