package org.diplom_back.modules.analytics.controller;

import org.diplom_back.modules.analytics.dto.AnalyticsDTO;
import org.diplom_back.modules.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public AnalyticsDTO getSummary() {
        return analyticsService.getStatistics();
    }
}