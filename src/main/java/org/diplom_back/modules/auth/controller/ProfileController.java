package org.diplom_back.modules.auth.controller;

import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.entity.Child;
import org.diplom_back.modules.auth.repository.UserRepository;
import org.diplom_back.modules.auth.repository.ClientRepository;
import org.diplom_back.modules.auth.repository.ChildRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, ClientRepository clientRepository, ChildRepository childRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.childRepository = childRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. ПОЛУЧИТЬ ВСЕ ДАННЫЕ (User + Client + Children)
    @GetMapping("/me")
    public ResponseEntity<?> getFullProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Благодаря FetchType и связям, user уже содержит в себе client и children
        return ResponseEntity.ok(user);
    }

    // 2. ОБНОВИТЬ ЛИЧНЫЕ ДАННЫЕ (Имя, Телефон)
    @PutMapping("/update")
    public ResponseEntity<?> updateClient(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody Client clientData) {
        User user = userRepository.findByEmail(userDetails.getUsername()).get();

        Client client = clientRepository.findByUserUserId(user.getUserId())
                .orElse(new Client()); // Если записи еще нет, создаем новую

        client.setUser(user);
        client.setFirstName(clientData.getFirstName());
        client.setLastName(clientData.getLastName());
        client.setPhoneNumber(clientData.getPhoneNumber());

        clientRepository.save(client);
        return ResponseEntity.ok("Данные клиента обновлены");
    }

    // 3. ДОБАВИТЬ РЕБЕНКА
    @PostMapping("/children")
    public ResponseEntity<?> addChild(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody Child child) {
        User user = userRepository.findByEmail(userDetails.getUsername()).get();
        child.setUser(user);
        return ResponseEntity.ok(childRepository.save(child));
    }
    @PutMapping("/children/{id}")
    public ResponseEntity<?> updateChild(@PathVariable Long id, @RequestBody Child childData) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ребенок не найден"));

        child.setName(childData.getName());
        child.setGender(childData.getGender());
        child.setBirthDate(childData.getBirthDate());

        childRepository.save(child);
        return ResponseEntity.ok("Данные ребенка обновлены");
    }
    // Изменение пароля
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> passwords) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // Проверка старого пароля (если используешь BCrypt)
        if (!passwordEncoder.matches(passwords.get("oldPassword"), user.getPassword())) {
            return ResponseEntity.badRequest().body("Старый пароль неверный");
        }

        user.setPassword(passwordEncoder.encode(passwords.get("newPassword")));
        userRepository.save(user);
        return ResponseEntity.ok("Пароль успешно изменен");
    }

    // Изменение логина (Email)
    @PutMapping("/change-email")
    public ResponseEntity<?> changeEmail(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> data) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        String newEmail = data.get("newEmail");

        if (userRepository.existsByEmail(newEmail)) {
            return ResponseEntity.badRequest().body("Этот Email уже занят");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        return ResponseEntity.ok("Email изменен. Перезайдите в систему.");
    }

    // Удаление ребенка
    @DeleteMapping("/children/{id}")
    public ResponseEntity<?> deleteChild(@PathVariable Long id) {
        childRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Полное удаление аккаунта
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        userRepository.delete(user);
        return ResponseEntity.ok("Аккаунт удален");
    }
}