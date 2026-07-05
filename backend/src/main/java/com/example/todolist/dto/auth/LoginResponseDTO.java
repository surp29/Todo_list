package com.example.todolist.dto.auth;

import com.example.todolist.dto.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response returned after a successful login, containing the JWT access token
 * and the authenticated user's profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private UserResponseDTO user;
}
