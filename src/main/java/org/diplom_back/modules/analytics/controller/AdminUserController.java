package org.diplom_back.modules.analytics.controller;

import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Здесь будут все поля, включая createdAt и email
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable String id, @RequestBody Map<String, String> payload) {
        // Ищем пользователя по строковому ID (UUID)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(User.Role.valueOf(payload.get("role")));
        userRepository.save(user);

        return ResponseEntity.ok("Role updated");
    }
}