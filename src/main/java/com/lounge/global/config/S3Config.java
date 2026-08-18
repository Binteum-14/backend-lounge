package com.lounge.global.config;

import com.lounge.global.config.properties.AppProperties;
import com.lounge.global.config.properties.AwsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, AppProperties.class})
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(AwsProperties awsProperties) {
        return S3Client.builder()
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
