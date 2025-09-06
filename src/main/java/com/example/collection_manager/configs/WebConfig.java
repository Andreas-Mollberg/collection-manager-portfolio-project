
package com.example.collection_manager.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadBaseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        configureImageUploads(registry);
        configureStaticResources(registry);
    }

    private void configureImageUploads(ResourceHandlerRegistry registry) {
        String imageUploadDir = Paths.get(uploadBaseDir, "images").toString();
        ensureDirectoryExists(imageUploadDir);

        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + imageUploadDir + File.separator)
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    private void configureStaticResources(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(86400)
                .resourceChain(true);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(86400)
                .resourceChain(true);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(86400)
                .resourceChain(true);

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400)
                .resourceChain(true);
    }

    private void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + directoryPath);
            } else {
                System.err.println("Failed to create upload directory: " + directoryPath);
            }
        }
    }
}