package puj.app.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Objects;

@Service
@Profile("s3")
@Slf4j
public class S3StorageService implements StorageService {

    @Value("${app.storage.s3.bucket}")
    private String bucket;

    @Value("${app.storage.s3.prefix:portal}")
    private String prefix;

    @Value("${app.aws.region:us-east-1}")
    private String region;

    // Opcional: solo si NO usas variables de entorno
    @Value("${app.aws.access-key-id:}")
    private String accessKeyId;

    @Value("${app.aws.secret-access-key:}")
    private String secretAccessKey;

    private S3Client s3;

    @PostConstruct
    void init() {
        var builder = S3Client.builder().region(Region.of(region));

        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    )
            );
            log.info("S3 creds: usando StaticCredentialsProvider (propiedades).");
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
            log.info("S3 creds: usando DefaultCredentialsProvider (env/IMDS).");
        }

        s3 = builder.build();
        log.info("S3 inicializado. Bucket={}, Prefix={}, Region={}", bucket, prefix, region);
    }

    @Override
    public String upload(String key, InputStream stream, long size, String contentType) {
        if (size < 0) throw new IllegalArgumentException("size requerido para RequestBody.fromInputStream");
        var normalizedPrefix = (prefix == null || prefix.isBlank()) ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
        var objectKey = normalizedPrefix + key;

        var req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(Objects.requireNonNullElse(contentType, "application/octet-stream"))
                .build();

        s3.putObject(req, RequestBody.fromInputStream(stream, size));

        String https = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
        return https;
    }
}
