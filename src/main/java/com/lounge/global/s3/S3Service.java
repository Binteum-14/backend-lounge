package com.lounge.global.s3;

import com.lounge.global.config.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public String uploadQrCode(byte[] imageBytes, String objectKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.s3().bucket())
                .key(objectKey)
                .contentType(MediaType.IMAGE_PNG_VALUE)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));
        return toPublicUrl(objectKey);
    }

    public String toPublicUrl(String objectKey) {
        return "https://" + awsProperties.s3().bucket()
                + ".s3." + awsProperties.region()
                + ".amazonaws.com/" + objectKey;
    }
}
