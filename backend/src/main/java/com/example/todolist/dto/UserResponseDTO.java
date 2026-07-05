package com.example.todolist.dto;

import com.example.todolist.model.Role;
import com.example.todolist.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response payload representing a user account (without the password).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String fullName;
    private String position;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponseDTO from(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .position(user.getPosition())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
