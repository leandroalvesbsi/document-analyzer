package com.visiblethread.documentanalyzer.config.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.secretKey}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean
    @Profile("!test")
    public S3Client s3Client() {

        final S3Client s3Client = S3Client.builder()
                .credentialsProvider(getCredentialsProvider())
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(awsRegion))
                .forcePathStyle(true)
                .build();

        createBucketIfNotExists(s3Client);

        return s3Client;
    }

    @Bean
    @Profile("test")
    public S3Client s3ClientTest() {
        return S3Client.builder().region(Region.of(awsRegion)).build();
    }

    private void createBucketIfNotExists(S3Client s3Client) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
            log.info("Bucket created: {}", createBucketRequest.bucket());
        } catch (S3Exception e) {
            log.error("Error creating bucket: {}", e.awsErrorDetails().errorMessage());
        }
    }

    private StaticCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey));
    }

}
