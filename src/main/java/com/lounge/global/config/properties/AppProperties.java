package com.lounge.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String publicUrl
) {

    public String normalizedPublicUrl() {
        if (publicUrl.endsWith("/")) {
            return publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl;
    }
}
