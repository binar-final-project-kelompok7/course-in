package com.github.k7.coursein.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "ddxh3d7rf",
            "api_key", "841123643285235",
            "api_secret", "vnCpZhqtA_i8TWKtwFK76xTNs_Q"));
    }

}
