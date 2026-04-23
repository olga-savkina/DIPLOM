package org.diplom_back.modules.auth.controller;

import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1. Проверяем, существует ли пользователь
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Пользователь с таким Email уже существует!");
        }

        // 2. Если нет — шифруем пароль и сохраняем
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.CLIENT);
        userRepository.save(user);

        return ResponseEntity.ok("Регистрация успешна!");
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Ищем пользователя в базе по имени (email) из объекта аутентификации
        return userRepository.findByEmail(authentication.getName())
                .map(user -> {
                    // Возвращаем только нужные данные (без пароля!)
                    Map<String, Object> data = new HashMap<>();
                    data.put("email", user.getEmail());
                    data.put("role", user.getRole());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}