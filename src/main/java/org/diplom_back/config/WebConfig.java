package org.diplom_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Все запросы, начинающиеся с /uploads/
        registry.addResourceHandler("/uploads/**")
                // Будут искать файлы в этой физической папке
                .addResourceLocations("file:F:/будущийдиплом/DIPLOM_PROGA/diplom_uploads");
    }
}