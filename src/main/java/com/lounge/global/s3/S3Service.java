package com.lounge.global.s3;

import com.lounge.global.config.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public String uploadQrCode(byte[] imageBytes, String objectKey) {
        String bucket = awsProperties.s3().bucket();
        String region = awsProperties.region();

        log.info("QR S3 업로드 시도. bucket={}, region={}, key={}, bytes={}",
                bucket, region, objectKey, imageBytes == null ? 0 : imageBytes.length);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(MediaType.IMAGE_PNG_VALUE)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(imageBytes));
        } catch (S3Exception e) {
            log.error(
                    "QR S3 PutObject 실패. bucket={}, region={}, key={}, status={}, errorCode={}, errorMessage={}",
                    bucket,
                    region,
                    objectKey,
                    e.statusCode(),
                    e.awsErrorDetails() == null ? null : e.awsErrorDetails().errorCode(),
                    e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage(),
                    e
            );
            throw e;
        } catch (SdkException e) {
            log.error(
                    "QR S3 업로드 SDK 실패. bucket={}, region={}, key={}, message={}",
                    bucket,
                    region,
                    objectKey,
                    e.getMessage(),
                    e
            );
            throw e;
        }

        String publicUrl = toPublicUrl(objectKey);
        log.info("QR S3 업로드 성공. url={}", publicUrl);
        return publicUrl;
    }

    public String toPublicUrl(String objectKey) {
        return "https://" + awsProperties.s3().bucket()
                + ".s3." + awsProperties.region()
                + ".amazonaws.com/" + objectKey;
    }
}
