package org.diplom_back.modules.analytics.controller;

import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize; // Если используете проверку ролей

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable String id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(User.Role.valueOf(payload.get("role")));
        userRepository.save(user);

        return ResponseEntity.ok("Role updated");
    }

    // --- НОВЫЙ МЕТОД УДАЛЕНИЯ ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        // Проверяем, существует ли пользователь перед удалением
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }

        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("Пользователь успешно удален");
        } catch (Exception e) {
            // Это может произойти, если на пользователя ссылаются другие таблицы (FK)
            return ResponseEntity.status(400)
                    .body("Не удалось удалить пользователя: возможно, у него есть активные заказы или отзывы.");
        }
    }
}