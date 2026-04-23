package org.diplom_back.modules.auth.controller;

import org.springframework.web.bind.annotation.*;


@RestController
public class TestController {


    @GetMapping("/hello")
    public String sayHello() {
        // 4. Это то, что отобразится прямо на странице в браузере
        return "Сервер BabyBoom запущен и готов к работе!";
    }
}
