package vallegrande.edu.pe.visons.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/profile-images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path location = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path parent = location.getParent() != null ? location.getParent() : location;
        // map /uploads/** to the parent directory (e.g. <project>/uploads/) so
        // requests like /uploads/profile-images/FILE resolve to the correct file
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(parent.toUri().toString() + "/");
    }
}