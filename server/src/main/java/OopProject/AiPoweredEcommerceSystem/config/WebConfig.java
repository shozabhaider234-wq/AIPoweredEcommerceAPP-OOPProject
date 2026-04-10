package OopProject.AiPoweredEcommerceSystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration.
 *
 * <p>Registers a static resource handler that maps the URL pattern
 * {@code /images/products/**} to the physical upload directory on disk.
 *
 * <p>This means that an image saved as:
 * <pre>
 *   uploads/products/abc123.jpg           ← physical file
 * </pre>
 * becomes accessible at:
 * <pre>
 *   http://localhost:8080/images/products/abc123.jpg   ← public URL
 * </pre>
 *
 * <p>The database stores the relative URL path ({@code /images/products/abc123.jpg})
 * so the frontend can construct the full URL by prepending the base URL.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Physical directory where uploaded files are saved.
     * Configured via {@code app.upload.dir} in application.properties.
     * Example value: {@code uploads/products}
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map HTTP URL path → physical filesystem path.
        // The "file:" prefix tells Spring to read from the filesystem,
        // not the classpath.
        registry
                .addResourceHandler("/images/products/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}

